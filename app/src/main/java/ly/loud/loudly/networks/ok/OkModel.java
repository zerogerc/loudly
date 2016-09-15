package ly.loud.loudly.networks.ok;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.application.models.KeysModel;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.exceptions.NetworkException;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;
import solid.collections.SolidList;

import static ly.loud.loudly.networks.Networks.OK;
import static ly.loud.loudly.util.ListUtils.asSolidList;

public class OkModel implements NetworkContract {
    private static final String REDIRECT_URI = "https://www.loud.ly";

    @NonNull
    private final Loudly loudly;

    @NonNull
    private final KeysModel keysModel;

    @NonNull
    private final OkClient client;

    @NonNull
    private final List<SinglePost> cached;

    @Inject
    public OkModel(@NonNull Loudly loudly, @NonNull KeysModel keysModel, @NonNull OkClient client) {
        this.loudly = loudly;
        this.keysModel = keysModel;
        this.client = client;
        cached = new ArrayList<>();
    }

    @Override
    @Network
    public int getId() {
        return OK;
    }

    @NonNull
    @Override
    public String getFullName() {
        return loudly.getString(R.string.network_ok);
    }

    @Override
    @DrawableRes
    public int getNetworkIconResource() {
        // ToDo: black icon
        return R.drawable.ic_ok_white;
    }

    @Override
    public int getBrandColorResourcePrimary() {
        // ToDo: get color
        return R.color.accent;
    }

    @NonNull
    @Override
    public Single<String> getBeginAuthUrl() {
        return Single.fromCallable(() -> new Query("https://connect.ok.ru/oauth/authorize")
                        .addParameter("client_id", OkClient.CLIENT_ID)
                        .addParameter("scope", "LONG_ACCESS_TOKEN.VALUABLE_ACCESS.PHOTO_CONTENT")
                        .addParameter("response_type", "token")
                        .addParameter("layout", "m")
                        .addParameter("redirect_uri", REDIRECT_URI)
                        .toURL()
        );
    }

    @NonNull
    @Override
    public Single<KeyKeeper> proceedAuthUrls(@NonNull Observable<String> urls) {
        return urls
                .filter(url -> url.startsWith(REDIRECT_URI))
                .map(url -> {
                    Query query = Query.fromResponseUrl(url);
                    if (query == null) {
                        throw Exceptions.propagate(new NetworkException(getId()));
                    }
                    String accessToken = query.getParameter("access_token");
                    String sessionKey = query.getParameter("session_secret_key");
                    if (accessToken == null || sessionKey == null) {
                        throw Exceptions.propagate(new NetworkException(getId()));
                    }
                    return (KeyKeeper) new OkKeyKeeper(accessToken, sessionKey);
                })
                .first()
                .toSingle();
    }

    @Override
    public boolean isConnected() {
        return keysModel.getOkKeyKeeper() != null;
    }

    @NonNull
    @Override
    public Completable disconnect() {
        return Completable.fromAction(cached::clear);
    }

    @NonNull
    @Override
    public Observable<SingleImage> upload(@NonNull PlainImage image) {
        // ToDO: implement
        return Observable.empty();
    }

    @NonNull
    @Override
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        // ToDo: implement
        return Observable.empty();
    }

    @NonNull
    @Override
    public Completable delete(@NonNull SinglePost post) {
        // ToDo: implement
        return Completable.complete();
    }

    @NonNull
    @Override
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        // ToDo: implement
        return Observable.empty();
    }

    @NonNull
    @Override
    public SolidList<SinglePost> getCachedPosts() {
        return asSolidList(cached);
    }

    @NonNull
    @Override
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element,
                                                    @RequestType int requestType) {
        // ToDo: implement
        return Observable.empty();
    }

    @NonNull
    @Override
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        // ToDo: implement
        return Observable.empty();
    }

    @NonNull
    @Override
    public Observable<List<Pair<SinglePost, Info>>> getUpdates(@NonNull SolidList<SinglePost> posts) {
        // ToDo: implement
        return Observable.empty();
    }

    @NonNull
    @Override
    public String getPersonPageUrl(@NonNull Person person) {
        // ToDo: implement
        return "ok.ru";
    }

    @NonNull
    @Override
    public Single<String> getCommentUrl(@NonNull Comment comment, @NonNull SinglePost post) {
        // ToDo: implement
        return Single.just("ok.ru");
    }
}
