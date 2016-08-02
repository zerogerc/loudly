package ly.loud.loudly.ui.brand_new.post;

import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.models.NetworkContract;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;
import ly.loud.loudly.new_base.plain.PlainImage;
import ly.loud.loudly.ui.brand_new.BasePresenter;
import solid.collections.SolidList;

public class NewPostPresenter extends BasePresenter<NewPostView> {

    @NonNull
    PostUploadModel postUploadModel;

    @NonNull
    private List<Attachment> attachments = new ArrayList<>();

    public NewPostPresenter(@NonNull PostUploadModel postUploadModel) {
        this.postUploadModel = postUploadModel;
    }

    public <T extends Fragment & NewPostView> void takePhoto(@NonNull T target) {
        RxPaparazzo.takeImage(target)
                .usingCamera()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        executeIfViewBound(NewPostView::showGalleryError);
                        return;
                    }

                    PlainImage image = new PlainImage(response.data(), new Point(0, 0));
                    attachments.add(image);

                    response.targetUI().showNewAttachment(image);
                });
    }

    public <T extends Fragment & NewPostView> void loadImageFromGallery(@NonNull T target) {
        RxPaparazzo.takeImage(target).useInternalStorage()
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        executeIfViewBound(NewPostView::showGalleryError);
                        return;
                    }

                    PlainImage image = new PlainImage(response.data(), new Point(0, 0));
                    attachments.add(image);

                    response.targetUI().showNewAttachment(image);
                });
    }

    public void uploadPost(@Nullable String text,
                           @NonNull SolidList<Attachment> attachments,
                           @NonNull List<NetworkContract> networks
    ) {
    }
}
