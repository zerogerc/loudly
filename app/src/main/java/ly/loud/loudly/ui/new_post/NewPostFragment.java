package ly.loud.loudly.ui.new_post;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.views.PostButton;
import ly.loud.loudly.ui.views.TextPlusAttachmentsView;

import static ly.loud.loudly.application.Loudly.getApplication;
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
public class NewPostFragment extends Fragment implements NewPostView {

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

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    NewPostPresenter presenter;

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplication(getContext()).getAppComponent().plus(new NewPostModule()).inject(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        assertActivityImplementsInterface(getActivity(), NewPostFragmentInteractions.class);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.material_new_post_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        presenter.onBindView(this);

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
    public void onDestroyView() {
        presenter.onUnbindView(this);
        unbinder.unbind();
        super.onDestroyView();
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

    @Module
    public static class NewPostModule {

        @Provides
        @NonNull
        public NewPostPresenter provideNewPostPresenter(
                @NonNull Loudly loudlyApplication,
                @NonNull PostUploadModel postUploadModel
        ) {
            return new NewPostPresenter(loudlyApplication, postUploadModel);
        }
    }

    @Subcomponent(modules = NewPostModule.class)
    public interface NewPostComponent {

        void inject(@NonNull NewPostFragment fragment);
    }
}
