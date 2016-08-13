package ly.loud.loudly.ui.new_post;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.networks.NetworkContract;
import solid.collections.SolidList;

@UiThread
public interface NetworksChooseView extends MvpView {
    void showModels(@NonNull SolidList<NetworkContract> list);
}
