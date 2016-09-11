package ly.loud.loudly.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import ly.loud.loudly.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ly.loud.loudly.util.AssertionsUtils.assertActivityImplementsInterface;

public final class LoadingFragment extends Fragment {
    private final int STATE_LOADING = 0;
    private final int STATE_ERROR = 1;

    @IntDef({STATE_LOADING, STATE_ERROR})
    public @interface LoadingState {
    }

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.loading_fragment_layout_progress_bar)
    @NonNull
    ProgressBar progressBar;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.loading_fragment_layout_error_message)
    @NonNull
    TextView errorView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.loading_fragment_layout_swipe_refresh)
    @NonNull
    SwipeRefreshLayout swipeRefreshLayout;

    @State
    @LoadingState
    int loadingState = STATE_LOADING;

    @Nullable
    private String errorMessage;

    @Nullable
    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        assertActivityImplementsInterface(getActivity(), LoadingFragmentCallback.class);
    }

    @Override
    @NonNull
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.loading_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        Icepick.restoreInstanceState(this, savedInstanceState);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.accent),
                ContextCompat.getColor(getContext(), R.color.primary)
        );
        swipeRefreshLayout.setOnRefreshListener(() -> ((LoadingFragmentCallback) getActivity()).onLoadingRefresh());
        updateView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    /**
     * Show error on the screen.
     * @param errorMessage - human readable error
     */
    public void showError(@StringRes int errorMessage) {
        loadingState = STATE_ERROR;
        this.errorMessage = getString(errorMessage);
        updateView();
    }

    /**
     * Show progress on the screen.
     */
    public void showLoading() {
        loadingState = STATE_LOADING;
        updateView();
    }

    public void hideProgress() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateView() {
        if (loadingState == STATE_LOADING) {
            progressBar.setVisibility(VISIBLE);
            swipeRefreshLayout.setVisibility(GONE);
        } else {
            progressBar.setVisibility(GONE);
            errorView.setText(errorMessage);
            swipeRefreshLayout.setVisibility(VISIBLE);
        }
    }

    public interface LoadingFragmentCallback {
        void onLoadingRefresh();
    }
}
