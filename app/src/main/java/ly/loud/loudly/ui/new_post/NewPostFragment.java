package ly.loud.loudly.ui.new_post;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.views.PostButton;
import ly.loud.loudly.ui.views.TextPlusAttachmentsView;

import static ly.loud.loudly.util.AssertionsUtils.assertActivityImplementsInterface;

/**
 * Fragment for creating and uploading post to networks. <br/>
 * Activity must implement interface {@link NetworksProvider} and provide this fragment with networks
 * that this fragment need to upload new post. <br/>
 * Activity could also implement {@link NewPostFragmentInteractions} to notify user about uploading progress.
 *
 * @see NetworksProvider
 * @see NewPostFragmentInteractions
 */
public class NewPostFragment extends MvpFragment<NewPostView, NewPostPresenter>
    implements NewPostView {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.material_new_post_fragment_send_button)
    @NonNull
    PostButton sendButton;

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
        Loudly.getApplication(getContext()).getAppComponent().inject(this);
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
        textPlusAttachmentsView.addOnEditTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    sendButton.setStateLight();
                } else {
                    sendButton.setStateBright();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        assertActivityImplementsInterface(activity, NewPostFragmentInteractions.class);
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

    /**
     * Triggered by presenter when loudlyPost uploaded to one more network.
     */
    @Override
    public void onPostUploadingProgress(@NonNull LoudlyPost loudlyPost) {
        // Just notify activity about progress
        ((NewPostFragmentInteractions) getActivity()).onPostUploadProgress(loudlyPost);
    }

    /**
     * Triggered by presenter when loudlyPost have been fully uploaded to all desired networks.
     */
    @Override
    public void onPostUploadCompleted() {
        ((NewPostFragmentInteractions) getActivity()).onPostUploaded();
    }

    @OnClick(R.id.material_new_post_fragment_send_button)
    public void onSendClicked() {
        List<NetworkContract> models = ((NetworksProvider) getActivity()).getChosenNetworks();
        String text = textPlusAttachmentsView.getText();
        List<Attachment> attachments = textPlusAttachmentsView.getAttachmentList();

        if (!(text.isEmpty() && attachments.isEmpty())) {
            presenter.uploadPost(text, attachments, models);
        }
        ((NewPostFragmentInteractions) getActivity()).onPostButtonClicked();
    }

    @OnClick(R.id.material_new_post_fragment_camera_button)
    public void onTakePhotoClicked() {
        presenter.takePhoto(this);
    }

    @OnClick(R.id.material_new_post_fragment_gallery_button)
    public void onPickFromGalleryClicker() {
        presenter.loadImageFromGallery(this);
    }

    @OnClick(R.id.material_new_post_fragment_networks_list_button)
    public void onShowNetworkClicked() {
        ((NetworksProvider) getActivity()).showNetworksChooseLayout();
    }

    /**
     * Interface for getting chosen networks. (networks to post to)
     */
    public interface NetworksProvider {
        void showNetworksChooseLayout();

        List<NetworkContract> getChosenNetworks();
    }

    /**
     * Interface to interact with {@link NewPostFragment}.
     */
    public interface NewPostFragmentInteractions {
        /**
         * Triggered when current loudlyPost uploaded to one more network.
         */
        void onPostUploadProgress(@NonNull LoudlyPost loudlyPost);

        /**
         * Triggered when current loudlyPost have been fully uploaded to all desired networks.
         */
        void onPostUploaded();

        /**
         * Triggered when user clicked on POST button to hide {@link NewPostFragment}
         */
        void onPostButtonClicked();
    }
}
