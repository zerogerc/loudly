package ly.loud.loudly.ui.new_post;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.ui.views.TextPlusAttachmentsView;

public class NewPostFragment extends MvpFragment<NewPostView, NewPostPresenter>
    implements NewPostView {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_send_button)
    @NonNull
    Button sendButton;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_text_plus_attachments)
    @NonNull
    TextPlusAttachmentsView textPlusAttachmentsView;

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
    public void showNewAttachment(@NonNull Attachment attachment) {
        textPlusAttachmentsView.addAttachment(attachment);
    }

    @Override
    public void showGalleryError() {

    }

    @OnClick(R.id.material_new_post_fragment_send_button)
    public void onSendClicked() {
        List<NetworkContract> models = ((NetworksProvider) getActivity()).getChosenNetworks();
        String text = textPlusAttachmentsView.getText();
        List<Attachment> attachments = textPlusAttachmentsView.getAttachmentList();

        if (!(text.isEmpty() && attachments.isEmpty())) {
            presenter.uploadPost(text, attachments, models);
        }
    }

    @OnClick(R.id.material_new_post_fragment_camera_button)
    public void onTakePhotoClicked() {
        presenter.takePhoto(this);
    }

    @OnClick(R.id.material_new_post_fragment_gallery_button)
    public void onPickFromGalleryClicker() {
        presenter.loadImageFromGallery(this);
    }

    /**
     * Interface for getting chosen networks. (networks to post to)
     */
    public interface NetworksProvider {
        List<NetworkContract> getChosenNetworks();
    }
}
