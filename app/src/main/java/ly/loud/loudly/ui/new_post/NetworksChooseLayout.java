package ly.loud.loudly.ui.new_post;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.hannesdorfmann.mosby.mvp.layout.MvpLinearLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.networks.NetworkContract;
import solid.collections.SolidList;

/**
 * Fragment that allows user to choose networks from all connected.
 *
 */
public class NetworksChooseLayout extends MvpLinearLayout<NetworksChooseView, NetworksChoosePresenter>
        implements NetworksChooseView {

    private static final String SUPER_STATE = "super_state";
    private static final String SELECTED = "selected";

    @SuppressWarnings("NullableProblems")
    @BindView(R.id.networks_choose_recycler_view)
    @NonNull
    RecyclerView recyclerView;

    @SuppressWarnings("NullableProblems") // onCreate
    @Inject
    @NonNull
    CoreModel coreModel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    NetworkChooseAdapter adapter;

    public NetworksChooseLayout(@NonNull Context context) {
        this(context, null);
    }

    public NetworksChooseLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworksChooseLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NetworksChooseLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Loudly.getContext().getAppComponent().inject(this);
        adapter = new NetworkChooseAdapter(getContext());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.bind(this);
        initRecyclerView();
    }

    @Override
    @NonNull
    public NetworksChoosePresenter createPresenter() {
        return new NetworksChoosePresenter(coreModel);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);
        showModels(presenter.getConnectedNetworks());
    }

    @Override
    public void showModels(@NonNull SolidList<NetworkContract> models) {
        adapter.setModels(models);
    }

    /**
     * @return List of networks currently chosen by user.
     */
    public List<NetworkContract> getChosenNetworks() {
        List<NetworkContract> chosen = new ArrayList<>();
        boolean[] selected = adapter.getSelectedNetworks();

        for (NetworkContract model : adapter.getModels()) {
            if (selected[model.getId()]) {
                chosen.add(model);
            }
        }
        return chosen;
    }

    @Override
    @NonNull
    public Parcelable onSaveInstanceState () {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        state.putBooleanArray(SELECTED, adapter.getSelectedNetworks());
        return state;
    }

    @Override
    public void onRestoreInstanceState (@NonNull Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle)state;
            boolean[] selected = savedState.getBooleanArray(SELECTED);
            if (selected != null) {
                adapter.setSelectedNetworks(selected);
            }
            Parcelable superState = savedState.getParcelable(SUPER_STATE);
            super.onRestoreInstanceState(superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
