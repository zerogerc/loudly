package ly.loud.loudly.networks.instagram;

import android.graphics.Point;
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
import ly.loud.loudly.networks.instagram.entities.Image;
import ly.loud.loudly.networks.instagram.entities.InstagramPost;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.NetworkUtils;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import retrofit2.Call;
import retrofit2.Response;
import rx.Completable;
import rx.Observable;
import rx.Single;
import solid.collections.SolidList;

import static ly.loud.loudly.util.ListUtils.asArrayList;
import static ly.loud.loudly.util.ListUtils.asSolidList;
import static ly.loud.loudly.util.ListUtils.emptyArrayList;
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
                        // ToDO: handle
                        return null;
                    }
                    String accessToken = query.getParameter("access_token");
                    if (accessToken == null) {
                        // ToDo: handle
                        return null;
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

    private List<SinglePost> downloadPosts(@Nullable SinglePost beforeId,
                                           @NonNull TimeInterval interval) throws IOException {
        InstagramKeyKeeper keyKeeper = keysModel.getInstagramKeyKeeper();
        if (keyKeeper == null) {
            // ToDo: handle
            return Collections.emptyList();
        }
        String maxId = beforeId == null ? "" : beforeId.getLink();
        Call<Data<List<InstagramPost>>> dataCall = client.loadPosts(maxId, keyKeeper.getAccessToken());
        List<SinglePost> posts = new ArrayList<>();
        long currentTime = 0;
        do {
            Log.i("INSTAGRAM", "DOWNLOADING");
            Response<Data<List<InstagramPost>>> executed = dataCall.execute();
            Data<List<InstagramPost>> body = executed.body();
            if (body == null || body.data == null) {
                return posts;
            }
            for (InstagramPost post : body.data) {
                currentTime = post.createdTime;
                if (!interval.contains(currentTime)) {
                    continue;
                }
                ArrayList<SingleAttachment> attachments;
                if (post.images != null) {
                    Image instagramImage = post.images.standardResolution;
                    SingleImage image = new SingleImage(
                            instagramImage.url,
                            new Point(instagramImage.width, instagramImage.height),
                            getId(),
                            instagramImage.url
                    );
                    attachments = asArrayList(image);
                } else {
                    attachments = emptyArrayList();
                }
                SinglePost singlePost = new SinglePost(
                        post.caption.text,
                        post.createdTime,
                        attachments,
                        null,
                        getId(),
                        post.id,
                        extractInfo(post)
                );
                posts.add(singlePost);
            }
            if (body.pagination == null || body.pagination.nextUrl == null) {
                break;
            }
            dataCall = client.continueLoadPostsWithPagination(body.pagination.nextUrl);
        } while (interval.contains(currentTime));
        return posts;
    }

    @NonNull
    private Info extractInfo(@NonNull InstagramPost post) {
        return new Info(post.likes.count, 0, post.comments.count);
    }

    @NonNull
    @Override
    public SolidList<SinglePost> getCachedPosts() {
        return SolidList.empty();
    }

    @Override
    @NonNull
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element,
                                                    @RequestType int requestType) {
        return Observable.just(SolidList.empty());
    }

    @Override
    @NonNull
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.just(null);
    }

    @NonNull
    @Override
    public Observable<List<Pair<SinglePost, Info>>> getUpdates(@NonNull SolidList<SinglePost> posts) {
        return Observable.empty();
    }

    @Override
    @NonNull
    public String getPersonPageUrl(@NonNull Person person) {
        return "";
    }
}
