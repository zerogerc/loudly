package ly.loud.loudly.application.models;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.networks.Facebook.FacebookClient;
import ly.loud.loudly.networks.Facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.Facebook.entities.*;
import ly.loud.loudly.new_base.*;
import ly.loud.loudly.networks.Facebook.FacebookAuthorizer;
import ly.loud.loudly.networks.Facebook.FacebookWrap;
import ly.loud.loudly.new_base.Comment;
import ly.loud.loudly.new_base.Person;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.Utils;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Single;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        return Observable.just(null);
    }

    @NonNull
    @Override
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.just(null);
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
    public Observable<List<Person>> getPersons(@NonNull SingleNetworkElement element, @RequestType int requestType) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                //ToDo: handle
                return Collections.emptyList();
            }
            String id = Link.getLink(element.getLink());
            if (id == null) {
                return Collections.emptyList();
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
                return Collections.emptyList();
            }
            for (FbPerson person : body.data) {
                ids.add(person.id);
            }

            Call<Map<String, FbPerson>> personsInfoCall =
                    client.getPersonsInfo(toCommaSeparated(ids), keyKeeper.getAccessToken());
            Response<Map<String, FbPerson>> executedPersonsCall = personsInfoCall.execute();
            Map<String, FbPerson> persons = executedPersonsCall.body();
            if (persons == null) {
                return Collections.emptyList();
            }
            List<Person> result = new ArrayList<>();
            for (String personId : ids) {
                result.add(toPerson(persons.get(personId)));
            }
            return result;
        });
    }

    @NonNull
    private SingleImage toImage(@NonNull Photo photo) {
        // ToDo: Strange ID
        return new SingleImage(photo.src, new Point(photo.width, photo.height), getId(), new Link(photo.src));
    }

    @Nullable
    private SingleAttachment toAttachment(@NonNull Attachment attachment) {
        if (attachment.media.image != null) {
            return toImage(attachment.media.image);
        }
        return null;
    }

    @NonNull
    @Override
    public Observable<List<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo: handle
                return Collections.emptyList();
            }
            Long since = timeInterval.from != Long.MIN_VALUE ? timeInterval.from : null;
            Long until = timeInterval.to != Long.MAX_VALUE ? timeInterval.to : null;
            Call<Data<List<Post>>> dataCall = client.loadPosts(since, until,
                    keyKeeper.getAccessToken());
            Response<Data<List<Post>>> execute = dataCall.execute();
            Data<List<Post>> body = execute.body();
            List<SinglePost> posts = new ArrayList<>();
            if (body == null) {
                return Collections.emptyList();
            }
            for (Post post : body.data) {
                ArrayList<SingleAttachment> attachments = new ArrayList<>();
                if (post.attachments != null) {
                    for (Attachment attachment : post.attachments.data) {
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
            return posts;
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
    public Observable<List<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.fromCallable(() -> {
            FacebookKeyKeeper keyKeeper = keysModel.getFacebookKeyKeeper();
            if (keyKeeper == null) {
                // ToDo handle
                return Collections.emptyList();
            }
            String link = Link.getLink(element.getLink());
            if (link == null) {
                // Here can't be comments

                return Collections.emptyList();
            }
            Call<Data<List<ly.loud.loudly.networks.Facebook.entities.Comment>>> dataCall =
                    client.loadComments(link, keyKeeper.getAccessToken());
            Response<Data<List<ly.loud.loudly.networks.Facebook.entities.Comment>>> executed = dataCall.execute();
            List<String> personIds = new ArrayList<>();
            for (ly.loud.loudly.networks.Facebook.entities.Comment comment : executed.body().data) {
                personIds.add(comment.from.id);
            }

            Call<Map<String, FbPerson>> personsInfoCall =
                    client.getPersonsInfo(toCommaSeparated(personIds), keyKeeper.getAccessToken());
            Response<Map<String, FbPerson>> personsCallExecuted =
                    personsInfoCall.execute();
            Map<String, FbPerson> persons = personsCallExecuted.body();

            List<Comment> comments = new ArrayList<>();
            for (ly.loud.loudly.networks.Facebook.entities.Comment comment : executed.body().data) {
                ArrayList<SingleAttachment> attachment = comment.attachment == null ? Utils.emptyArrayList() :
                        Utils.asArrayList(toAttachment(comment.attachment));

                comments.add(new Comment(comment.message, comment.createdTime, attachment, toPerson(persons.get(comment.from.id)),
                        getId(), new Link(comment.id)));
            }
            return comments;
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

    @Override
    public int getId() {
        return Networks.FB;
    }
}
