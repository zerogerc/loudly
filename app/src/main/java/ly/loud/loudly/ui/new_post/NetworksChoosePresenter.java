package ly.loud.loudly.ui.new_post;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.BasePresenter;
import rx.schedulers.Schedulers;
import solid.collections.SolidList;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class NetworksChoosePresenter extends BasePresenter<NetworksChooseView> {

    @NonNull
    private CoreModel coreModel;

    public NetworksChoosePresenter(@NonNull CoreModel coreModel) {
        this.coreModel = coreModel;
    }

    public SolidList<NetworkContract> getConnectedNetworks() {
        return coreModel.getNetworkModels();
    }

    public void loadModels() {
        List<NetworkContract> list = new ArrayList<>();
        coreModel.observeAllNetworksModels()
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .doOnNext(list::add)
                .doOnCompleted(() -> executeIfViewBound(view -> view.showModels(list)))
                .subscribe();
    }
}
