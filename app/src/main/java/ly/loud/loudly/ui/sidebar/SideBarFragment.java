package ly.loud.loudly.ui.sidebar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;

import static ly.loud.loudly.application.Loudly.getApplication;

public class SideBarFragment extends Fragment {

    @SuppressWarnings("NullableProblems") // onViewCreated
    @BindView(R.id.sidebar_layout_recycler)
    @NonNull
    private RecyclerView recyclerView;

    @Nullable
    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplication(getContext()).getAppComponent().plus(new SideBarModule()).inject(this);
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
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    @Module
    public static class SideBarModule {
        @Provides
        @NonNull
        public SideBarPresenter provideSideBarPresenter(
                @NonNull Loudly loudlyApplication,
                @NonNull CoreModel coreModel
        ) {
            return new SideBarPresenter(loudlyApplication, coreModel);
        }
    }

    @Subcomponent(modules = SideBarModule.class)
    public interface SideBarComponent {

        void inject(@NonNull SideBarFragment fragment);
    }
}
