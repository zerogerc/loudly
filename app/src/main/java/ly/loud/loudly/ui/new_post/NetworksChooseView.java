package ly.loud.loudly.ui.new_post;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import ly.loud.loudly.networks.NetworkContract;

@UiThread
public interface NetworksChooseView extends MvpView {
    void showModels(@NonNull List<NetworkContract> list);
}
