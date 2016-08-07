package ly.loud.loudly.application.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.facebook.FacebookModel;
import ly.loud.loudly.networks.instagram.InstagramModel;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.vk.VKModel;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.legacy_base.SingleNetwork;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import rx.Observable;

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
