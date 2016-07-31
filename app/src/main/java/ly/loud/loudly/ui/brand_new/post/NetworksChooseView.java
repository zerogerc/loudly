package ly.loud.loudly.ui.brand_new.post;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import ly.loud.loudly.application.models.NetworkContract;

@UiThread
public interface NetworksChooseView extends MvpView {
    void showModels(@NonNull List<NetworkContract> list);
}
