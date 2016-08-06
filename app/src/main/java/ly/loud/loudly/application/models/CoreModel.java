package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.new_base.KeyKeeper;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import rx.Observable;
import rx.Single;

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

    public Observable<NetworkContract> getAllNetworksModels() {
        return Observable.from(networkModels);
    }

    public Observable<NetworkContract> getConnectedNetworksModels() {
        return getAllNetworksModels().filter(NetworkContract::isConnected);
    }


    /**
     * Get models where given {@link SingleNetwork} exists in.
     */
    public Observable<NetworkContract> elementExistsIn(@NonNull SingleNetworkElement element) {
        return getConnectedNetworksModels().filter(network -> element.getNetwork() == network.getId());
    }

    public Observable<NetworkContract> elementExistsIn(@NonNull MultipleNetworkElement element) {
        return getConnectedNetworksModels()
                .filter(networkContract ->  element.getSingleNetworkInstance(networkContract.getId()) != null);
    }

    public Observable<Boolean> connectToNetworkById(@Network int networkId,
                                                    @NonNull KeyKeeper keyKeeper) {
        for (NetworkContract model : networkModels) {
            if (model.getId() == networkId) {
                return model.connect(keyKeeper);
            }
        }
        return Observable.just(false);
    }
}
