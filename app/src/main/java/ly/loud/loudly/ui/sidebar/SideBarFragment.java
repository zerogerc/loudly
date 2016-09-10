package ly.loud.loudly.ui.sidebar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import icepick.Icepick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.networks.loudly.LoudlyModel;
import solid.collections.SolidList;

import static ly.loud.loudly.application.Loudly.getApplication;
import static ly.loud.loudly.util.AssertionsUtils.assertActivityImplementsInterface;

public class SideBarFragment extends Fragment implements SideBarView {

    @SuppressWarnings("NullableProblems") // onViewCreated
    @BindView(R.id.sidebar_layout_recycler)
    @NonNull
    RecyclerView recyclerView;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    SideBarPresenter presenter;

    @Nullable
    private Unbinder unbinder;

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private SideBarAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplication(getContext()).getAppComponent().plus(new SideBarModule()).inject(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        assertActivityImplementsInterface(getActivity(), SideBarFragmentCallbacks.class);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sidebar_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        presenter.onBindView(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SideBarAdapter(getContext(), (SideBarFragmentCallbacks) getActivity());
        Icepick.restoreInstanceState(adapter, savedInstanceState);
        recyclerView.setAdapter(adapter);

        presenter.loadNetworks();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(adapter, outState);
    }

    @Override
    public void onDestroyView() {
        presenter.onUnbindView(this);
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    @Override
    public void onNetworksLoaded(@NonNull SolidList<NetworkContract> networkContracts) {
        adapter.setNetworks(networkContracts);
    }

    public interface SideBarFragmentCallbacks {
        void onNoFiltersClicked();

        void onSettingsClicked();

        void onNetworkClicked(@Network int networkId);
    }

    @Module
    public static class SideBarModule {
        @Provides
        @NonNull
        public SideBarPresenter provideSideBarPresenter(
                @NonNull Loudly loudlyApplication,
                @NonNull CoreModel coreModel,
                @NonNull LoudlyModel loudlyModel
                ) {
            return new SideBarPresenter(loudlyApplication, coreModel, loudlyModel);
        }
    }

    @Subcomponent(modules = SideBarModule.class)
    public interface SideBarComponent {

        void inject(@NonNull SideBarFragment fragment);
    }
}
