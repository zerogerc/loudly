package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.VK.VKKeyKeeper;
import rx.Single;

/**
 * Created by ZeRoGerc on 24/07/16.
 */
public class KeysModel {

    @NonNull
    private Loudly loudlyApplication;

    @Nullable
    private VKKeyKeeper vkKeyKeeper;

    @Inject
    public KeysModel(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    @CheckResult
    public VKKeyKeeper getVKKeyKeeper() {
        return vkKeyKeeper;
    }

    public void setVKKeyKeeper(@NonNull VKKeyKeeper keyKeeper) {
        vkKeyKeeper = keyKeeper;
    }

    @CheckResult
    public Single<Boolean> disconnectFromNetwork(int network) {
        return Single.just(true);
    }
}
