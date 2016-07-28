package ly.loud.loudly.ui.brand_new.post;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.NetworkContract;

/**
 * Fragment that allows user to choose networks from all connected.
 * @see onNetworksChooseListener
 */
public class NetworksChooseFragment extends MvpFragment<NetworksChooseView, NetworksChoosePresenter>
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

    @NonNull
    private final List<Boolean> result = new ArrayList<>();

    @Nullable
    private onNetworksChooseListener onNetworksChooseListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.network_choose_fragment_new, container, false);
    }

    @Override
    @NonNull
    public NetworksChoosePresenter createPresenter() {
        return new NetworksChoosePresenter(coreModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NetworkChooseAdapter(getContext(), models);
        adapter.setOnItemStateChangeListener((index, state) -> result.set(index, state));
        recyclerView.setAdapter(adapter);

        presenter.loadModels();
    }

    public void setOnNetworksChooseListener(@Nullable NetworksChooseFragment.onNetworksChooseListener onNetworksChooseListener) {
        this.onNetworksChooseListener = onNetworksChooseListener;
    }

    @Override
    public void showModels(@NonNull List<NetworkContract> list) {
        models.addAll(list);
        for (int i = 0; i < list.size(); i++) {
            result.add(false);
        }
        adapter.notifyDataSetChanged();
    }

    public interface onNetworksChooseListener {
        void onNetworksChoosen(List<NetworkContract> models);
    }
}
