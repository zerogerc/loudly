package ly.loud.loudly.ui.sidebar;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.loudly.LoudlyModel;
import ly.loud.loudly.util.BasePresenter;
import ly.loud.loudly.util.ListUtils;

public class SideBarPresenter extends BasePresenter<SideBarView> {

    @NonNull
    private CoreModel coreModel;

    @NonNull
    private LoudlyModel loudlyModel;

    public SideBarPresenter(
            @NonNull Loudly loudlyApplication,
            @NonNull CoreModel coreModel,
            @NonNull LoudlyModel loudlyModel
    ) {
        super(loudlyApplication);
        this.coreModel = coreModel;
        this.loudlyModel = loudlyModel;
    }

    public void loadNetworks() {
        final ArrayList<NetworkContract> list = new ArrayList<>();
        list.add(loudlyModel);
        coreModel.getAllNetworkModels().forEach(list::add);

        executeIfViewBound(view -> view.onNetworksLoaded(ListUtils.asSolidList(list)));
    }
}
