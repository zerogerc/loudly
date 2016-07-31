package ly.loud.loudly.ui.brand_new.post;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

import java.util.List;

import ly.loud.loudly.application.models.NetworkContract;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.ui.brand_new.BasePresenter;
import solid.collections.SolidList;

public class NewPostPresenter extends BasePresenter<NewPostView> {

    @NonNull
    PostUploadModel postUploadModel;

    public NewPostPresenter(@NonNull PostUploadModel postUploadModel) {
        this.postUploadModel = postUploadModel;
    }

    public void makePhoto() {

    }

    public <T extends Fragment & NewPostView> void loadImageFromGallery(@NonNull T target) {
        Log.e("NEWPOSTRX", "PRE");
        RxPaparazzo.takeImage(target)
                .usingCamera()
                .subscribe(response -> {
                    Log.e("NEWPOSTRX", "IMAGE");
                    if (response.resultCode() != Activity.RESULT_OK) {
                        executeIfViewBound(NewPostView::showGalleryError);
                        return;
                    }

                    // TODO: save to list of attachments
                    response.targetUI().showImage(response.data());
                });
    }

    public void uploadPost(@Nullable String text,
                           @NonNull SolidList<Attachment> attachments,
                           @NonNull List<NetworkContract> networks
    ) {
    }
}
