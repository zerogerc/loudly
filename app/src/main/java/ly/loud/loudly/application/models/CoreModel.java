package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.networks.facebook.FacebookModel;
import ly.loud.loudly.networks.instagram.InstagramModel;
import ly.loud.loudly.networks.ok.OkModel;
import ly.loud.loudly.networks.vk.VKModel;
import rx.Observable;
import solid.collections.SolidList;

import static ly.loud.loudly.util.ListUtils.asSolidList;
import static solid.collectors.ToSolidList.toSolidList;

public class CoreModel {
    @NonNull
    private Loudly loudlyApplication;

    @NonNull
    private List<NetworkContract> networkModels;

    public CoreModel(
            @NonNull Loudly loudlyApplication,
            @NonNull FacebookModel facebookModel,
            @NonNull VKModel vkModel,
            @NonNull InstagramModel instagramModel,
            @NonNull OkModel okModel
    ) {
        this.loudlyApplication = loudlyApplication;

        this.networkModels = new ArrayList<>();
        networkModels.add(facebookModel);
        networkModels.add(vkModel);
        networkModels.add(instagramModel);
        networkModels.add(okModel);
    }

    @CheckResult
    @Nullable
    public NetworkContract getModelByNetwork(@Network int network) {
        for (NetworkContract networkContract : networkModels) {
            if (networkContract.getId() == network) {
                return networkContract;
            }
        }
        return null;
    }

    @CheckResult
    @NonNull
    public Observable<NetworkContract> observeAllNetworksModels() {
        return Observable.from(networkModels);
    }

    @NonNull
    public SolidList<NetworkContract> getAllNetworkModels() {
        return asSolidList(networkModels);
    }

    @CheckResult
    @NonNull
    public Observable<NetworkContract> observeConnectedNetworksModels() {
        return observeAllNetworksModels().filter(NetworkContract::isConnected);
    }

    @NonNull
    public SolidList<NetworkContract> getConnectedNetworksModels() {
        return getAllNetworkModels().filter(NetworkContract::isConnected).collect(toSolidList());
    }

    /**
     * Get models where given {@link SingleNetworkElement} exists in.
     */
    @CheckResult
    @NonNull
    public Observable<NetworkContract> elementExistsIn(@NonNull SingleNetworkElement element) {
        return observeConnectedNetworksModels().filter(network -> element.getNetwork() == network.getId());
    }

    @CheckResult
    @NonNull
    public Observable<NetworkContract> elementExistsIn(@NonNull MultipleNetworkElement element) {
        return observeConnectedNetworksModels()
                .filter(networkContract -> element.getSingleNetworkInstance(networkContract.getId()) != null);
    }
}
