package ly.loud.loudly.ui.new_post;

import android.support.annotation.NonNull;

import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.BasePresenter;
import solid.collections.SolidList;

public class NetworksChoosePresenter extends BasePresenter<NetworksChooseView> {

    @NonNull
    private CoreModel coreModel;

    public NetworksChoosePresenter(@NonNull CoreModel coreModel) {
        this.coreModel = coreModel;
    }

    public SolidList<NetworkContract> getConnectedNetworks() {
        // TODO: connected networks
        return coreModel.getAllNetworkModels();
    }
}
