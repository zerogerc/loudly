package ly.loud.loudly.networks.facebook;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Link;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.facebook.entities.*;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.TimeInterval;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import solid.collections.SolidList;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FacebookModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private KeysModel keysModel;

    @NonNull
    private FacebookClient client;

    @Nullable
    private FacebookAuthorizer authorizer;

    @Nullable
    private FacebookWrap wrap;

    @Inject
    public FacebookModel(@NonNull Loudly loudlyApplication,
                         @NonNull KeysModel keysModel,
                         @NonNull FacebookClient client) {
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
        this.client = client;
    }

    @NonNull
    @Override
    public Observable<Boolean> reset() {
        return Observable.just(true);
    }

    public FacebookWrap getWrap() {
        if (wrap == null) {
            this.wrap = new FacebookWrap();
        }
        return wrap;
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

    @NonNull
    @Override
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: handle

                return null;
            }
            String attachmentId = post.getAttachments().isEmpty() ? null :
                    Link.getLink(post.getAttachments().get(0).getLink());

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
            return new SinglePost(post.getText(), post.getDate(), post.getAttachments(), post.getLocation(),
                    getId(), new Link(id));
        });
    }

    @NonNull
    @Override
    public Observable<Boolean> delete(@NonNull SinglePost post) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: Handle
                return false;
            }
            String id = Link.getLink(post.getLink());
            if (id == null) {
                // Nothing to delete
                return true;
            }

            Call<Result> resultCall = client.deleteElement(id, keyKeeper.getAccessToken());
            Response<Result> execute = resultCall.execute();
            Result body = execute.body();
            if (body == null) {
                return false;
            }
            return body.success;
        });
    }

    @NonNull
    @Override
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                //ToDo: handle
                return SolidList.empty();
            }
            String id = Link.getLink(element.getLink());
            if (id == null) {
                return SolidList.empty();
            }
            String endpoint;
            switch (requestType) {
                case GetterModel.LIKES:
                    endpoint = FacebookClient.LIKES_ENDPOINT;
                    break;
                case GetterModel.SHARES:
                    endpoint = FacebookClient.REPOSTS_ENDPOINT;
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
                    client.getPersonsInfo(toCommaSeparated(ids), keyKeeper.getAccessToken());
            Response<Map<String, FbPerson>> executedPersonsCall = personsInfoCall.execute();
            Map<String, FbPerson> persons = executedPersonsCall.body();
            if (persons == null) {
                return SolidList.empty();
            }
            List<Person> result = new ArrayList<>();
            for (String personId : ids) {
                result.add(toPerson(persons.get(personId)));
            }
            return ListUtils.asSolidList(result);
        });
    }

    private Map<String, SingleImage> getImageInfos(List<String> images) throws IOException {
        FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
        if (keyKeeper == null) {
            return Collections.emptyMap();
        }
        Call<Map<String, Picture>> pictureInfos =
                client.getPictureInfos(toCommaSeparated(images), keyKeeper.getAccessToken());
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
        return new SingleImage(photo.src, new Point(photo.width, photo.height), getId(), new Link(photo.src));
    }

    @NonNull
    private SingleImage toImage(@NonNull Picture photo, @NonNull String id) {
        return new SingleImage(id, new Point(photo.width, photo.height), getId(), new Link(id));
    }

    @Nullable
    private SingleAttachment toAttachment(@NonNull FbAttachment attachment) {
        if (attachment.media.image != null) {
            return toImage(attachment.media.image);
        }
        return null;
    }

    @NonNull
    @Override
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: handle
                return SolidList.empty();
            }
            Long since = timeInterval.from != Long.MIN_VALUE ? timeInterval.from : null;
            Long until = timeInterval.to != Long.MAX_VALUE ? timeInterval.to : null;
            Call<Data<List<Post>>> dataCall = client.loadPosts(since, until,
                    keyKeeper.getAccessToken());
            Response<Data<List<Post>>> execute = dataCall.execute();
            Data<List<Post>> body = execute.body();
            List<SinglePost> posts = new ArrayList<>();
            if (body == null) {
                return SolidList.empty();
            }
            for (Post post : body.data) {
                ArrayList<SingleAttachment> attachments = new ArrayList<>();
                if (post.attachments != null) {
                    for (FbAttachment attachment : post.attachments.data) {
                        SingleAttachment parsed = toAttachment(attachment);
                        if (parsed != null) {
                            attachments.add(parsed);
                        }
                    }
                }
                int likes = post.likes != null ? post.likes.summary.totalCount : 0;
                int shares = post.shares != null ? post.shares.count : 0;
                int comments = post.comments != null ? post.comments.summary.totalCount : 0;
                posts.add(new SinglePost(post.message, post.createdTime, attachments, null,
                        getId(), new Link(post.id), new Info(likes, shares, comments)));
            }
            return ListUtils.asSolidList(posts);
        });
    }

    @NonNull
    private String toCommaSeparated(@NonNull List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            if (id.indexOf('_') != -1) id = id.substring(0, id.indexOf("_"));
            sb.append(id);
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return ids.toString();
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
        return new Person(person.firstName, person.lastName, url, getId());
    }

    @NonNull
    @Override
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo handle
                return SolidList.empty();
            }
            String link = Link.getLink(element.getLink());
            if (link == null) {
                // Here can't be comments

                return SolidList.empty();
            }
            Call<Data<List<FbComment>>> dataCall =
                    client.loadComments(link, keyKeeper.getAccessToken());
            Response<Data<List<FbComment>>> executed = dataCall.execute();
            List<String> personIds = new ArrayList<>();
            for (FbComment comment : executed.body().data) {
                personIds.add(comment.from.id);
            }

            Call<Map<String, FbPerson>> personsInfoCall =
                    client.getPersonsInfo(toCommaSeparated(personIds), keyKeeper.getAccessToken());
            Response<Map<String, FbPerson>> personsCallExecuted =
                    personsInfoCall.execute();
            Map<String, FbPerson> persons = personsCallExecuted.body();

            List<Comment> comments = new ArrayList<>();
            for (FbComment comment : executed.body().data) {
                ArrayList<SingleAttachment> attachment = comment.attachment == null ? ListUtils.emptyArrayList() :
                        ListUtils.asArrayList(toAttachment(comment.attachment));

                comments.add(new Comment(comment.message, comment.createdTime, attachment, toPerson(persons.get(comment.from.id)),
                        getId(), new Link(comment.id)));
            }
            return ListUtils.asSolidList(comments);
        });
    }

    @NonNull
    @Override
    public Observable<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        if (!(keyKeeper instanceof FacebookKeyKeeper)) {
            throw new IllegalArgumentException("Wrong keykeeper");
        }
        keysModel.setFacebookKeyKeeper(((FacebookKeyKeeper) keyKeeper));
        return Observable.just(true);
    }

    @NonNull
    @Override
    public Observable<Boolean> disconnect() {
        return keysModel.disconnectFromNetwork(getId());
    }

    @NonNull
    @Override
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_facebook);
    }

    @Override
    public boolean isConnected() {
        return keysModel.getFacebookKeyKeeper() != null;
    }

    @Networks.Network
    @Override
    public int getId() {
        return Networks.FB;
    }
}
