package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.vk.VKKeyKeeper;
import ly.loud.loudly.networks.Networks.Network;
import rx.Observable;

/**
 * Created by ZeRoGerc on 24/07/16.
 */
public class KeysModel {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private VKKeyKeeper vkKeyKeeper;

    private FacebookKeyKeeper facebookKeyKeeper;

    @Inject
    public KeysModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @CheckResult
    @Nullable
    public VKKeyKeeper getVKKeyKeeper() {
        return vkKeyKeeper;
    }

    public void setVKKeyKeeper(@Nullable VKKeyKeeper keyKeeper) {
        vkKeyKeeper = keyKeeper;
    }

    @CheckResult
    @Nullable
    public FacebookKeyKeeper getFacebookKeyKeeper() {
        return facebookKeyKeeper;
    }

    public void setFacebookKeyKeeper(@Nullable FacebookKeyKeeper facebookKeyKeeper) {
        this.facebookKeyKeeper = facebookKeyKeeper;
    }

    @CheckResult
    @NonNull
    public Observable<Boolean> disconnectFromNetwork(@Network int network) {
        return Observable.just(true);
    }
}
