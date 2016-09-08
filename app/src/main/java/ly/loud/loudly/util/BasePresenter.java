package ly.loud.loudly.util;

import android.support.annotation.AnyThread;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;

import ly.loud.loudly.application.Loudly;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Base class for all presenters.
 * @param <V> - view to be bounded to
 */
public class BasePresenter<V> {

    /**
     * Base application Context.
     */
    @NonNull
    private Loudly loudlyApplication;

    /**
     * Bound view or null if no view is currently bounded to this presenter.
     */
    @Nullable
    private volatile V view;

    /**
     * {@link Subscription Subscriptions} that will be unbounded in {@link #onUnbindView(V)}.
     */
    @NonNull
    private final CompositeSubscription unsubscribeOnUnbindViewSubscriptions = new CompositeSubscription();

    public BasePresenter(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    /**
     * Call when you need to bind view to presenter.
     */
    @UiThread
    @CallSuper
    public void onBindView(@NonNull V view) {
        V previousView = this.view;

        if (previousView != null) {
            throw new IllegalStateException("Previous view is not unbounded.");
        }

        this.view = view;
    }

    /**
     * Call then you need to unbind view from presenter.
     * Typically it occurred in {@link Fragment#onDestroyView()} before super method invocation.
     */
    @UiThread
    @CallSuper
    public void onUnbindView(@NonNull V view) {
        final V previousView = this.view;

        unsubscribeOnUnbindViewSubscriptions.clear();
        if (previousView == view) {
            this.view = null;
        } else {
            throw new IllegalStateException("onBindView() and onUnbindView() should be invoked on the same view");
        }
    }

    /**
     * Check if any view is attached to this presenter
     */
    protected boolean isViewAttached() {
        return getView() != null;
    }

    /**
     * Returns currently bounded view or null if no view is bounded.
     *
     * @see {@link #isViewAttached()}
     */
    @Nullable
    protected V getView() {
        return this.view;
    }

    /**
     * Executes action on the view if view is not-null. Otherwise just ignore the action.
     * You may assume that <code>>view</code> is not-null in action clojure.
     */
    @AnyThread
    protected void executeIfViewBound(@NonNull Action1<V> action) {
        final V currentView = getView();

        if (currentView != null) {
            action.call(currentView);
        }
    }

    /**
     * Guarantees that given action will be unbounded in {@link #onUnbindView(V)}
     */
    @AnyThread
    protected final void unsubscribeOnUnbindView(@NonNull Subscription subscription) {
        unsubscribeOnUnbindViewSubscriptions.add(subscription);
    }
}
