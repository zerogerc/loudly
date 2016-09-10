package ly.loud.loudly.networks.facebook;

import android.graphics.Point;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.networks.facebook.entities.Data;
import ly.loud.loudly.networks.facebook.entities.Element;
import ly.loud.loudly.networks.facebook.entities.ElementId;
import ly.loud.loudly.networks.facebook.entities.FbAttachment;
import ly.loud.loudly.networks.facebook.entities.FbComment;
import ly.loud.loudly.networks.facebook.entities.FbPerson;
import ly.loud.loudly.networks.facebook.entities.Photo;
import ly.loud.loudly.networks.facebook.entities.Picture;
import ly.loud.loudly.networks.facebook.entities.Post;
import ly.loud.loudly.networks.facebook.entities.Result;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.NetworkUtils.DividedList;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Completable;
import rx.Observable;
import rx.Single;
import solid.collections.SolidList;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;
import static ly.loud.loudly.util.ListUtils.asSolidList;
import static ly.loud.loudly.util.ListUtils.removeByPredicateInPlace;
import static ly.loud.loudly.util.NetworkUtils.divideListOfCachedPosts;
import static solid.collectors.ToList.toList;

public class FacebookModel implements NetworkContract {
    public static final String AUTHORIZE_URL = "https://www.facebook.com/dialog/oauth";
    public static final String RESPONSE_URL = "https://web.facebook.com/connect/login_success.html";
    public static final String REDIRECT_URL = "https://www.facebook.com/connect/login_success.html";

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private KeysModel keysModel;

    @NonNull
    private FacebookClient client;

    @NonNull
    private final List<SinglePost> cached;

    @Inject
    public FacebookModel(@NonNull Loudly loudlyApplication,
                         @NonNull KeysModel keysModel,
                         @NonNull FacebookClient client) {
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
        this.client = client;
        cached = new ArrayList<>();
    }

    @Network
    @Override
    public int getId() {
        return Networks.FB;
    }

    @Override
    @NonNull
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_facebook);
    }

    @Override
    public int getNetworkIconResource() {
        return R.drawable.facebook_icon_black;
    }

    @ColorRes
    @Override
    public int getBrandColorResourcePrimary() {
        return R.color.facebook_color;
    }

    @Override
    @NonNull
    public Single<String> getBeginAuthUrl() {
        return Single.fromCallable(() -> new Query(AUTHORIZE_URL)
                .addParameter("client_id", FacebookClient.CLIENT_ID)
                .addParameter("redirect_uri", REDIRECT_URL)
                .addParameter("scope", "publish_actions,user_posts")
                .addParameter("response_type", "token")
                .toURL());
    }

    @Override
    @NonNull
    public Single<KeyKeeper> proceedAuthUrls(@NonNull Observable<String> urls) {
        return urls
                .takeFirst(url -> url.startsWith(REDIRECT_URL) ||
                        url.startsWith(RESPONSE_URL))
                .toSingle()
                .map(url -> {
                    Query response = Query.fromResponseUrl(url);
                    if (response == null) {
                        // ToDo: Handle
                        return null;
                    }
                    String accessToken = response.getParameter("access_token");
                    if (accessToken == null) {
                        // ToDo: Handle
                        return null;
                    }
                    return new FacebookKeyKeeper(accessToken);
                });
    }

    @Override
    public boolean isConnected() {
        return keysModel.getFacebookKeyKeeper() != null;
    }

    @Override
    @NonNull
    public Completable disconnect() {
        return Completable.fromAction(cached::clear);
    }

    @NonNull
    @Override
    public Observable<SingleImage> upload(@NonNull PlainImage image) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: handle
                return null;
            }
            String url = image.getUrl();
            if (url == null) {
                return null;
            }
            File file = new File(image.getUrl());

            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("source", file.getName(), requestBody);
            Call<ElementId> elementIdCall = client.uploadPhoto(part, keyKeeper.getAccessToken());
            Response<ElementId> execute = elementIdCall.execute();
            if (execute == null) {
                return null;
            }
            ElementId id = execute.body();
            if (id == null) {
                return null;
            }
            Map<String, SingleImage> images = getImageInfos(Collections.singletonList(id.id));
            return images.get(id.id);
        });
    }

    @Override
    @NonNull
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: handle

                return null;
            }
            String attachmentId = post.getAttachments().isEmpty() ? null :
                    post.getAttachments().get(0).getLink();

            Call<ElementId> elementIdCall =
                    client.uploadPost(post.getText(), attachmentId, keyKeeper.getAccessToken());
            Response<ElementId> execute = elementIdCall.execute();
            if (execute == null) {
                return null;
            }
            ElementId id = execute.body();
            if (id == null) {
                return null;
            }
            SinglePost uploaded = new SinglePost(
                    post.getText(),
                    post.getDate(),
                    post.getAttachments(),
                    post.getLocation(),
                    getId(),
                    id.id
            );
            cached.add(0, uploaded);
            return uploaded;
        });
    }

    @Override
    @NonNull
    public Completable delete(@NonNull SinglePost post) {
        return Completable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return false;
            }
            String id = post.getLink();

            Call<Result> resultCall = client.deleteElement(id, keyKeeper.getAccessToken());
            Response<Result> execute = resultCall.execute();
            Result body = execute.body();
            if (body == null) {
                return false;
            }
            if (body.success) {
                removeByPredicateInPlace(cached, somePost ->
                        somePost.getLink().equals(post.getLink()));
                return true;
            }
            return false;
        });
    }

    @Override
    @NonNull
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                //ToDo: handle
                return SolidList.empty();
            }
            String id = element.getLink();
            String endpoint;
            switch (requestType) {
                case LIKES:
                    endpoint = FacebookClient.LIKES_ENDPOINT;
                    break;
                case SHARES:
                    endpoint = FacebookClient.SHARES_ENDPOINT;
                    break;
                default:
                    endpoint = "";
            }
            Call<Data<List<FbPerson>>> likesOrSharesCall =
                    client.getLikesOrShares(id, endpoint, keyKeeper.getAccessToken());
            Response<Data<List<FbPerson>>> executed =
                    likesOrSharesCall.execute();
            List<String> ids = new ArrayList<>();
            Data<List<FbPerson>> body = executed.body();
            if (body == null) {
                return SolidList.empty();
            }
            for (FbPerson person : body.data) {
                ids.add(person.id);
            }

            Call<Map<String, FbPerson>> personsInfoCall =
                    client.getPersonsInfo(toCommaSeparatedUserIds(ids), keyKeeper.getAccessToken());
            Response<Map<String, FbPerson>> executedPersonsCall = personsInfoCall.execute();
            Map<String, FbPerson> persons = executedPersonsCall.body();
            if (persons == null) {
                return SolidList.empty();
            }
            List<Person> result = new ArrayList<>();
            for (String personId : ids) {
                result.add(toPerson(persons.get(personId)));
            }
            return asSolidList(result);
        });
    }

    @NonNull
    private Map<String, SingleImage> getImageInfos(List<String> images) throws IOException {
        FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
        if (keyKeeper == null) {
            return Collections.emptyMap();
        }
        Call<Map<String, Picture>> pictureInfos =
                client.getPictureInfos(toCommaSeparatedUserIds(images), keyKeeper.getAccessToken());
        Response<Map<String, Picture>> executed = pictureInfos.execute();
        if (executed == null) {
            return Collections.emptyMap();
        }
        Map<String, Picture> response = executed.body();
        if (response == null) {
            return Collections.emptyMap();
        }
        Map<String, SingleImage> result = new HashMap<>();
        for (Map.Entry<String, Picture> entry : response.entrySet()) {
            result.put(entry.getKey(), toImage(entry.getValue(), entry.getKey()));
        }
        return result;
    }

    @NonNull
    private SingleImage toImage(@NonNull Photo photo) {
        // ToDo: Strange ID
        return new SingleImage(photo.src, new Point(photo.width, photo.height), getId(), photo.src);
    }

    @NonNull
    private SingleImage toImage(@NonNull Picture photo, @NonNull String id) {
        return new SingleImage(id, new Point(photo.width, photo.height), getId(), id);
    }

    @Nullable
    private SingleAttachment toAttachment(@NonNull FbAttachment attachment) {
        if (attachment.media.image != null) {
            return toImage(attachment.media.image);
        }
        return null;
    }

    @Override
    @NonNull
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.fromCallable(() -> {
            if (cached.isEmpty()) {
                List<SinglePost> posts = downloadPosts(timeInterval);
                cached.addAll(posts);
                return asSolidList(posts);
            }
            DividedList<SinglePost> dividedList = divideListOfCachedPosts(cached, timeInterval);

            List<SinglePost> before = downloadPosts(dividedList.before);
            List<SinglePost> after = downloadPosts(dividedList.after);

            cached.addAll(before);
            cached.addAll(after);
            Collections.sort(cached);

            List<SinglePost> result = new ArrayList<>();
            result.addAll(before);
            result.addAll(dividedList.cached);
            result.addAll(after);
            return asSolidList(result);
        });
    }

    @NonNull
    @Override
    public SolidList<SinglePost> getCachedPosts() {
        return asSolidList(cached);
    }

    @NonNull
    private List<SinglePost> downloadPosts(@NonNull TimeInterval timeInterval) throws IOException {
        if (timeInterval.from >= timeInterval.to) {
            return Collections.emptyList();
        }
        Log.i("FACEBOOK", "DOWNLOADING");
        FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
        if (keyKeeper == null) {
            // ToDo: handle
            return Collections.emptyList();
        }
        Long since = timeInterval.from != Long.MIN_VALUE ? timeInterval.from : null;
        Long until = timeInterval.to != Long.MAX_VALUE ? timeInterval.to : null;
        List<SinglePost> posts = new ArrayList<>();
        Call<Data<List<Post>>> dataCall = client.loadPosts(since, until,
                keyKeeper.getAccessToken());
        long currentTime = 0;
        do {
            Response<Data<List<Post>>> execute = dataCall.execute();
            Data<List<Post>> body = execute.body();
            if (body == null) {
                return posts;
            }
            for (Post post : body.data) {
                currentTime = post.createdTime;
                if (currentTime < timeInterval.from) {
                    continue;
                }
                if (currentTime > timeInterval.to) {
                    break;
                }
                ArrayList<SingleAttachment> attachments = new ArrayList<>();
                if (post.attachments != null) {
                    for (FbAttachment attachment : post.attachments.data) {
                        SingleAttachment parsed = toAttachment(attachment);
                        if (parsed != null) {
                            attachments.add(parsed);
                        }
                    }
                    posts.add(new SinglePost(post.message, post.createdTime, attachments, null,
                            getId(), post.id, getInfo(post)));
                }
            }
            if (body.paging != null && body.paging.next != null) {
                dataCall = client.continueLoadPostsWithPagination(body.paging.next);
            } else {
                break;
            }
        } while (timeInterval.contains(currentTime));
        return posts;
    }

    @NonNull
    private Info getInfo(@NonNull Post post) {
        int likes = post.likes != null ? post.likes.summary.totalCount : 0;
        int shares = post.shares != null ? post.shares.count : 0;
        int comments = post.comments != null ? post.comments.summary.totalCount : 0;
        return new Info(likes, shares, comments);
    }

    @NonNull
    private String toCommaSeparatedUserIds(@NonNull List<String> ids) {
        List<String> userIDs = new ArrayList<>();
        for (String id : ids) {
            if (id.indexOf('_') != -1) {
                userIDs.add(id.substring(0, id.indexOf("_")));
            } else {
                userIDs.add(id);
            }
        }
        return toCommaSeparated(userIDs);
    }

    @NonNull
    private String toCommaSeparated(@NonNull List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            sb.append(id);
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    @NonNull
    private Person toPerson(@NonNull FbPerson person) {
        Data<Element> photo = person.picture;
        String url;
        if (photo == null) {
            url = null;
        } else {
            url = photo.data.url;
        }
        String firstName = person.firstName == null ? "" : person.firstName;
        String lastName = person.lastName == null ? "" : person.lastName;
        return new Person(firstName, lastName, url, getId(), person.id);
    }

    @Override
    @NonNull
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo handle
                return SolidList.empty();
            }
            String link = element.getLink();

            Call<Data<List<FbComment>>> dataCall =
                    client.loadComments(link, keyKeeper.getAccessToken());
            Response<Data<List<FbComment>>> executed = dataCall.execute();
            List<String> personIds = new ArrayList<>();
            for (FbComment comment : executed.body().data) {
                personIds.add(comment.from.id);
            }

            Call<Map<String, FbPerson>> personsInfoCall =
                    client.getPersonsInfo(toCommaSeparatedUserIds(personIds), keyKeeper.getAccessToken());
            Response<Map<String, FbPerson>> personsCallExecuted =
                    personsInfoCall.execute();
            Map<String, FbPerson> persons = personsCallExecuted.body();

            List<Comment> comments = new ArrayList<>();
            for (FbComment comment : executed.body().data) {
                ArrayList<SingleAttachment> attachment = comment.attachment == null ? ListUtils.emptyArrayList() :
                        ListUtils.asArrayList(toAttachment(comment.attachment));

                comments.add(new Comment(comment.message, comment.createdTime, attachment, toPerson(persons.get(comment.from.id)),
                        getId(), comment.id));
            }
            return asSolidList(comments);
        });
    }

    @CheckResult
    @Override
    @NonNull
    public Observable<List<Pair<SinglePost, Info>>> getUpdates(@NonNull SolidList<SinglePost> posts) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return null;
            }
            List<String> ids = posts.map(SinglePost::getLink).collect(toList());
            Call<Map<String, Post>> infoCall =
                    client.getInfo(toCommaSeparated(ids), keyKeeper.getAccessToken());
            Response<Map<String, Post>> executed = infoCall.execute();
            Map<String, Post> body = executed.body();
            if (body == null) {
                // ToDo: handle
                return null;
            }
            List<Pair<SinglePost, Info>> events = new ArrayList<>();
            for (SinglePost post : posts) {
                if (!body.containsKey(post.getLink())) {
                    continue;
                }
                Post got = body.get(post.getLink());
                Info newInfo = getInfo(got);
                Info difference = newInfo.subtract(post.getInfo());
                if (!difference.isEmpty()) {
                    // Update info in cached posts
                    for (int i = 0; i < cached.size(); i++) {
                        if (cached.get(i).getLink().equals(post.getLink())) {
                            cached.set(i, cached.get(i).setInfo(newInfo));
                            break;
                        }
                    }
                    events.add(new Pair<>(post, difference));
                }
            }
            return events;
        });
    }

    @Override
    @NonNull
    public String getPersonPageUrl(@NonNull Person person) {
        return "https://www.facebook.com/" + person.getId();
    }
}
