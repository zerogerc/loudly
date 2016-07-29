package ly.loud.loudly.ui.brand_new.post;

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
 *
 * @see onNetworksChooseListener
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

    @NonNull
    private final List<Boolean> result = new ArrayList<>();

    @Nullable
    private onNetworksChooseListener onNetworksChooseListener;

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

    public void setOnNetworksChooseListener(@Nullable NetworksChooseLayout.onNetworksChooseListener onNetworksChooseListener) {
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
