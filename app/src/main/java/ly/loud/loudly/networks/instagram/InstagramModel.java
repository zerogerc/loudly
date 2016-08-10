package ly.loud.loudly.networks.instagram;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.application.models.KeysModel;
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
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import rx.Single;
import solid.collections.SolidList;

public class InstagramModel implements NetworkContract {
    public static final String AUTHORIZE_URL = "https://api.instagram.com/oauth/authorize/";
    public static final String RESPONSE_URL = "loudly://";

    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private KeysModel keysModel;

    @Inject
    public InstagramModel(@NonNull Loudly loudlyApplication,
                          @NonNull KeysModel keysModel) {
        this.loudlyApplication = loudlyApplication;
        this.keysModel = keysModel;
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
                .takeFirst(url -> url.startsWith(RESPONSE_URL))
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
        return false;
    }

    @Override
    @NonNull
    public Single<Boolean> disconnect() {
        return Single.just(false);
    }

    @Override
    @NonNull
    public Observable<SingleImage> upload(@NonNull PlainImage image) {
        return Observable.just(null);
    }

    @Override
    @NonNull
    public Observable<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Observable.just(null);
    }

    @Override
    @NonNull
    public Observable<Boolean> delete(@NonNull SinglePost post) {
        return Observable.just(false);
    }

    @Override
    @NonNull
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.just(SolidList.empty());
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
}
