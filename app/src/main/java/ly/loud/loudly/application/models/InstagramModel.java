package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.Instagram.InstagramAuthorizer;
import ly.loud.loudly.networks.Instagram.InstagramKeyKeeper;
import ly.loud.loudly.networks.Instagram.InstagramWrap;
import ly.loud.loudly.new_base.*;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.TimeInterval;
import rx.Observable;
import solid.collections.SolidList;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class InstagramModel implements NetworkContract {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private InstagramKeyKeeper keyKeeper;

    @Nullable
    private InstagramAuthorizer authorizer;

    @Nullable
    private InstagramWrap wrap;

    @Inject
    public InstagramModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @NonNull
    @Override
    public Observable<Boolean> reset() {
        return Observable.just(true);
    }

    public InstagramWrap getWrap() {
        if (wrap == null) {
            this.wrap = new InstagramWrap();
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
        return Observable.just(false);
    }

    @NonNull
    @Override
    public Observable<SolidList<SinglePost>> loadPosts(@NonNull TimeInterval timeInterval) {
        return Observable.just(SolidList.empty());
    }

    @NonNull
    @Override
    public Observable<SolidList<Person>> getPersons(@NonNull SingleNetworkElement element, @GetterModel.RequestType int requestType) {
        return Observable.just(SolidList.empty());
    }

    @NonNull
    @Override
    public Observable<SolidList<Comment>> getComments(@NonNull SingleNetworkElement element) {
        return Observable.just(null);
    }

    @NonNull
    @Override
    public Observable<Boolean> connect(@NonNull KeyKeeper keyKeeper) {
        return Observable.just(false);
    }

    @NonNull
    @Override
    public Observable<Boolean> disconnect() {
        return Observable.just(false);
    }

    @NonNull
    @Override
    public String getFullName() {
        return loudlyApplication.getString(R.string.network_instagram);
    }

    @Override
    public boolean isConnected() {
        // ToDo: fix
        return true;
    }

    @Networks.Network
    @Override
    public int getId() {
        return Networks.INSTAGRAM;
    }
}
