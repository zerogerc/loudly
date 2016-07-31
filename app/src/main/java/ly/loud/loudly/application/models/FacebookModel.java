package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.networks.Facebook.FacebookAuthorizer;
import ly.loud.loudly.networks.Facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.Facebook.FacebookWrap;
import ly.loud.loudly.new_base.Comment;
import ly.loud.loudly.new_base.SingleImage;
import ly.loud.loudly.new_base.SinglePost;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.TimeInterval;
import rx.Single;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class FacebookModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private FacebookKeyKeeper keyKeeper;

    @Nullable
    private FacebookAuthorizer authorizer;

    @Nullable
    private FacebookWrap wrap;

    @Inject
    public FacebookModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @NonNull
    @Override
    public Single<Boolean> reset() {
        return Single.just(true);
    }

    public FacebookWrap getWrap() {
        if (wrap == null) {
            this.wrap = new FacebookWrap();
        }
        return wrap;
    }

    @NonNull
    @Override
    public Single<SingleImage> upload(@NonNull PlainImage image) {
        return Single.just(null);
    }

    @NonNull
    @Override
    public Single<SinglePost> upload(@NonNull PlainPost<SingleAttachment> post) {
        return Single.just(null);
    }

    @NonNull
    @Override
    public Single<Boolean> delete(@NonNull SinglePost post) {
        return Single.just(false);
    }

    @NonNull
    @Override
    public Single<List<Person>> getPersons(@NonNull SingleNetworkElement element, @GetterModel.RequestType int requestType) {
        return Single.just(Collections.emptyList());
    }

    @NonNull
    @Override
    public Single<List<PlainPost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Single.just(Collections.emptyList());
    }

    @NonNull
    @Override
    public Single<List<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Single.just(Collections.emptyList());
    }

    @NonNull
    @Override
    public Single<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        return Single.just(false);
    }

    @NonNull
    @Override
    public Single<Boolean> disconnect() {
        return Single.just(false);
    }

    @NonNull
    @Override
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_facebook);
    }

    @Override
    public boolean isConnected() {
        // ToDo: fix
        return true;
    }

    @Override
    public int getId() {
        return Networks.FB;
    }
}
