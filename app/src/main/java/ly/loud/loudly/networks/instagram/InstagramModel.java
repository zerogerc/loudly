package ly.loud.loudly.networks.instagram;

import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.exceptions.NetworkException;
import ly.loud.loudly.base.exceptions.NoTokenException;
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
import ly.loud.loudly.networks.instagram.entities.Data;
import ly.loud.loudly.networks.instagram.entities.InstagramComment;
import ly.loud.loudly.networks.instagram.entities.InstagramPerson;
import ly.loud.loudly.networks.instagram.entities.InstagramPost;
import ly.loud.loudly.util.NetworkUtils;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import retrofit2.Call;
import retrofit2.Response;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;
import solid.collections.SolidList;

import static ly.loud.loudly.util.ListUtils.asSolidList;
import static ly.loud.loudly.util.NetworkUtils.divideListOfCachedPosts;

public class InstagramModel implements NetworkContract {
    public static final String AUTHORIZE_URL = "https://api.instagram.com/oauth/authorize/";
    public static final String RESPONSE_URL = "loudly://";
    public static final String REDIRECT_URL = "loudly:";

    @NonNull
    private final InstagramClient client;

    @NonNull
    private final Loudly loudlyApplication;

    @NonNull
    private final KeysModel keysModel;

    @NonNull
    private final List<SinglePost> cached;

    @Inject
    public InstagramModel(@NonNull InstagramClient client,
                          @NonNull Loudly loudlyApplication,
                          @NonNull KeysModel keysModel) {
        this.client = client;
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
        cached = new ArrayList<>();
    }

    @Networks.Network
    @Override
    public int getId() {
        return Networks.INSTAGRAM;
    }

    @Override
    @NonNull
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_instagram);
    }

    @Override
    public int getNetworkIconResource() {
        return R.drawable.instagram_icon_black;
    }

    @ColorRes
    @Override
    public int getBrandColorResourcePrimary() {
        return R.color.instagram_color;
    }

    @Override
    @NonNull
    public Single<String> getBeginAuthUrl() {
        return Single.fromCallable(() ->
                new Query(AUTHORIZE_URL)
                        .addParameter("client_id", InstagramClient.CLIENT_ID)
                        .addParameter("redirect_uri", RESPONSE_URL)
                        .addParameter("response_type", "token")
                        .addParameter("scope", "basic public_content")
                        .toURL());
    }

    @Override
    @NonNull
    public Single<KeyKeeper> proceedAuthUrls(@NonNull Observable<String> urls) {
        return urls
                .takeFirst(url -> url.startsWith(REDIRECT_URL))
                .toSingle()
                .map(url -> {
                    Query query = Query.fromResponseUrl(url);
                    if (query == null) {
                        throw Exceptions.propagate(new NetworkException(getId()));
                    }
                    String accessToken = query.getParameter("access_token");
                    if (accessToken == null) {
                        throw Exceptions.propagate(new NetworkException(getId()));
                    }
                    return new InstagramKeyKeeper(accessToken);
                });
    }

    @Override
    public boolean isConnected() {
        return keysModel.getInstagramKeyKeeper() != null;
    }

    @Override
    @NonNull
    public Completable disconnect() {
        return Completable.complete();
    }

    @Override
    @NonNull
    public Observable<SingleImage> upload(@NonNull PlainImage image) {
        return Observable.empty();
    }

    @Override
    @NonNull
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.empty();
    }

    @Override
    @NonNull
    public Completable delete(@NonNull SinglePost post) {
        return Completable.complete();
    }

    @Override
    @NonNull
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.fromCallable(() -> {
            if (cached.isEmpty()) {
                List<SinglePost> loaded = downloadPosts(null, timeInterval);
                cached.addAll(loaded);
                return asSolidList(cached);
            }
            NetworkUtils.DividedList<SinglePost> dividedList =
                    divideListOfCachedPosts(cached, timeInterval);

            List<SinglePost> before = downloadPosts(null, dividedList.before);
            List<SinglePost> after = downloadPosts(cached.get(cached.size() - 1), dividedList.after);

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
    private List<SinglePost> downloadPosts(@Nullable SinglePost beforeId,
                                           @NonNull TimeInterval interval) throws IOException {
        InstagramKeyKeeper keyKeeper = keysModel.getInstagramKeyKeeper();
        if (keyKeeper == null) {
            throw new NoTokenException(getId());
        }
        String maxId = beforeId == null ? "" : beforeId.getLink();
        Call<Data<List<InstagramPost>>> dataCall = client.loadPosts(maxId, keyKeeper.getAccessToken());
        List<SinglePost> posts = new ArrayList<>();
        long currentTime = 0;
        do {
            Log.i("INSTAGRAM", "DOWNLOADING");
            Response<Data<List<InstagramPost>>> executed = dataCall.execute();
            Data<List<InstagramPost>> body = executed.body();
            if (body.isError()) {
                //noinspection ConstantConditions If it's error, then it can be converted to exception
                throw body.meta.getException();
            }
            //noinspection ConstantConditions If it doesn't has error, it has data
            for (InstagramPost post : body.data) {
                currentTime = post.createdTime;
                if (!interval.contains(currentTime)) {
                    continue;
                }

                posts.add(post.toPost());
            }
            if (body.pagination == null || body.pagination.nextUrl == null) {
                break;
            }
            dataCall = client.continueLoadPostsWithPagination(body.pagination.nextUrl);
        } while (interval.contains(currentTime));
        return posts;
    }

    @NonNull
    @Override
    public SolidList<SinglePost> getCachedPosts() {
        return asSolidList(cached);
    }

    @Override
    @NonNull
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element,
                                                    @RequestType int requestType) {
        return Observable.fromCallable(() -> {
            InstagramKeyKeeper keyKeeper = keysModel.getInstagramKeyKeeper();
            if (keyKeeper == null) {
                throw new NoTokenException(getId());
            }
            // There is no shares in Instagram
            Call<Data<List<InstagramPerson>>> likers =
                    client.getLikers(element.getLink(), keyKeeper.getAccessToken());
            Response<Data<List<InstagramPerson>>> execute = likers.execute();
            Data<List<InstagramPerson>> body = execute.body();
            if (body.isError()) {
                //noinspection ConstantConditions errors can be converted to exceptions
                throw body.meta.getException();
            }
            List<Person> persons = new ArrayList<>();
            //noinspection ConstantConditions If body has no error, it has data
            for (InstagramPerson instagramPerson : body.data) {
                persons.add(instagramPerson.toPerson());
            }
            return asSolidList(persons);
        });
    }

    @Override
    @NonNull
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.fromCallable(() -> {
            InstagramKeyKeeper keyKeeper = keysModel.getInstagramKeyKeeper();
            if (keyKeeper == null) {
                throw new NoTokenException(getId());
            }
            Call<Data<List<InstagramComment>>> getComments =
                    client.getComments(element.getLink(), keyKeeper.getAccessToken());
            Response<Data<List<InstagramComment>>> execute = getComments.execute();
            Data<List<InstagramComment>> body = execute.body();
            if (body.isError()) {
                //noinspection ConstantConditions errors can be transformed to exceptions
                throw body.meta.getException();
            }
            List<Comment> comments = new ArrayList<>();
            //noinspection ConstantConditions If body has no error, it has data
            for (InstagramComment comment : body.data) {
                comments.add(comment.toComment());
            }
            return asSolidList(comments);
        });
    }

    @NonNull
    @Override
    public Observable<List<Pair<SinglePost, Info>>> getUpdates(@NonNull SolidList<SinglePost> posts) {
        // ToDo: update
        return Observable.empty();
    }

    @Override
    @NonNull
    public String getPersonPageUrl(@NonNull Person person) {
        return "https://www.instagram.com/" + person.getId();
    }

    @NonNull
    @Override
    public Single<String> getCommentUrl(@NonNull Comment comment, @NonNull SinglePost post) {
        return Single.fromCallable(() -> {
            InstagramKeyKeeper keyKeeper = keysModel.getInstagramKeyKeeper();
            if (keyKeeper == null) {
                return "";
            }
            Call<Data<InstagramPost>> getPost =
                    client.getPost(post.getLink(), keyKeeper.getAccessToken());
            Response<Data<InstagramPost>> execute = getPost.execute();
            Data<InstagramPost> body = execute.body();
            if (body.data != null) {
                return body.data.link;
            }
            return "";
        }).onErrorReturn(error -> "");
    }
}
