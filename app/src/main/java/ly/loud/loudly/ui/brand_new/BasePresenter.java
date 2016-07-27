package ly.loud.loudly.ui.brand_new;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;

import solid.functions.Action1;

/**
 * Created by ZeRoGerc on 28/07/16.
 */
public class BasePresenter<V extends MvpView> extends MvpBasePresenter<V> {
    public void executeIfViewBound(Action1<V> action) {
        if (isViewAttached()) {
            action.call(getView());
        }
    }
}
