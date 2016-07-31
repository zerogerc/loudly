package ly.loud.loudly.ui.brand_new.post;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hannesdorfmann.mosby.mvp.MvpFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.NetworkContract;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.base.attachments.Attachment;
import solid.collections.SolidList;

public class NewPostFragment extends MvpFragment<NewPostView, NewPostPresenter>
    implements NewPostView {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_send_button)
    @NonNull
    Button sendButton;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_edit_text)
    @NonNull
    EditText postText;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_image)
    @NonNull
    ImageView imageView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_gallery_button)
    @NonNull
    View galleryButton;

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
        return inflater.inflate(R.layout.material_new_post_fragment, container, false);
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

    @Override
    public void showImage(@NonNull String url) {
        Glide.with(this).load(url).into(imageView);
    }

    @Override
    public void showNewAttachment(@NonNull Attachment attachment) {

    }

    @Override
    public void showGalleryError() {

    }

    @OnClick(R.id.material_new_post_fragment_send_button)
    public void onSendClicked() {
        // Activity knows the networks chosen by user
        List<NetworkContract> models = ((NetworksGetter) getActivity()).getChoosenNetworks();

        presenter.uploadPost(
                postText.getText().toString(),
                SolidList.empty(),
                models
                );
    }

    @OnClick(R.id.material_new_post_fragment_gallery_button)
    public void onPickFromGallery() {
        Log.e("NEWPOSTRX", "CLICK");
        presenter.loadImageFromGallery(this);
    }

    /**
     * Interface for getting chosen networks. (networks to post to)
     */
    public interface NetworksGetter {
        List<NetworkContract> getChoosenNetworks();
    }
}
