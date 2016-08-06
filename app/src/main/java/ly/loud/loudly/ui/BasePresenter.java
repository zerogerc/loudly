package ly.loud.loudly.ui;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;

import solid.functions.Action1;

public class BasePresenter<V extends MvpView> extends MvpBasePresenter<V> {
    public void executeIfViewBound(Action1<V> action) {
        if (isViewAttached()) {
            action.call(getView());
        }
    }
}
