package ly.loud.loudly.ui.brand_new;

import android.support.annotation.NonNull;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.Collections;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;

// TODO: config
public class FeedPresenter extends MvpBasePresenter<FeedView> {

    @NonNull
    private Loudly loudlyApp;

    @NonNull
    private GetterModel getterModel;

    public FeedPresenter(
            @NonNull Loudly loudlyApp,
            @NonNull GetterModel getterModel
    ) {
        this.loudlyApp = loudlyApp;
        this.getterModel = getterModel;
    }

    public void loadPosts() {
        // TODO: load from model
        if (isViewAttached()) {
            //noinspection ConstantConditions
            getView().showLoadedPosts(Collections.emptyList());
        }
    }
}
