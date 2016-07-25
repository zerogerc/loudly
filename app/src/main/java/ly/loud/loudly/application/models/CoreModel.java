package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.KeyKeeper;
import rx.Observable;
import rx.Single;

/**
 * Created by ZeRoGerc on 21/07/16.
 */
public class CoreModel {
    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private List<NetworkContract> networkModels;

    public CoreModel(
            @NonNull Loudly loudlyApplication,
            @NonNull FacebookModel facebookModel,
            @NonNull VKModel vkModel,
            @NonNull InstagramModel instagramModel
    ) {
        this.loudlyApplication = loudlyApplication;

        this.networkModels = new ArrayList<>();
        networkModels.add(facebookModel);
        networkModels.add(vkModel);
        networkModels.add(instagramModel);
    }

    public Observable<NetworkContract> getNetworksModels() {
        return Observable.from(networkModels).filter(NetworkContract::isConnected);
    }

    public Single<Boolean> connectToNetworkById(int id, @NonNull KeyKeeper keyKeeper) {
        for (NetworkContract model : networkModels) {
            if (model.getId() == id) {
                return model.connect(keyKeeper);
            }
        }
        return Single.fromCallable(() -> false);
    }
}
