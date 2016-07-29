package ly.loud.loudly.ui.brand_new.post;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.util.Utils;

public class NewPostFragment extends MvpFragment<NewPostView, NewPostPresenter> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.new_post_send_button)
    @NonNull
    ImageView sendButton;

    @SuppressWarnings("NullableProblems") // Butterknife
    @Inject
    @NonNull
    PostUploadModel postUploadModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_post_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    @NonNull
    public NewPostPresenter createPresenter() {
        return new NewPostPresenter(postUploadModel);
    }

    @OnClick(R.id.new_post_send_button)
    public void onSendClicked() {
        Utils.hidePhoneKeyboard(getActivity());
        // TODO: remove this crunch
        //noinspection ConstantConditions
        getView().postDelayed(() -> {
            NetworkChooseFragment dialog = new NetworkChooseFragment();
            dialog.show(getFragmentManager(), dialog.getTag());
        }, 50);
    }
}
