package ly.loud.loudly.ui.new_post;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.hannesdorfmann.mosby.mvp.layout.MvpLinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.networks.NetworkContract;

/**
 * Fragment that allows user to choose networks from all connected.
 *
 */
public class NetworksChooseLayout extends MvpLinearLayout<NetworksChooseView, NetworksChoosePresenter>
        implements NetworksChooseView {

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

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private final List<NetworkContract> models = new ArrayList<>();

    // Has the same size as models
    @NonNull
    private final List<Boolean> result = new ArrayList<>();

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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.bind(this);
        initRecyclerView();
        presenter.loadModels();
    }

    @Override
    @NonNull
    public NetworksChoosePresenter createPresenter() {
        return new NetworksChoosePresenter(coreModel);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NetworkChooseAdapter(getContext(), models);
        adapter.setOnItemStateChangeListener(result::set);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showModels(@NonNull List<NetworkContract> list) {
        models.addAll(list);
        result.addAll(Collections.nCopies(list.size(), false));
        adapter.notifyDataSetChanged();
    }

    /**
     * @return List of networks currently chosen by user.
     */
    public List<NetworkContract> getChosenNetworks() {
        List<NetworkContract> chosen = new ArrayList<>();
        for (int i = 0, size = result.size(); i < size; i++) {
            if (result.get(i)) {
                chosen.add(models.get(i));
            }
        }
        return chosen;
    }
}
