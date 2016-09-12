package ly.loud.loudly.ui.new_post;

import android.app.Activity;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.util.BasePresenter;
import ly.loud.loudly.util.ListUtils;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class NewPostPresenter extends BasePresenter<NewPostView> {

    @NonNull
    PostUploadModel postUploadModel;

    @NonNull
    private List<Attachment> attachments = new ArrayList<>();

    public NewPostPresenter(
            @NonNull Loudly loudlyApplication,
            @NonNull PostUploadModel postUploadModel
    ) {
        super(loudlyApplication);
        this.postUploadModel = postUploadModel;
    }

    public <T extends Fragment & NewPostView> void takePhoto(@NonNull T target) {
        unsubscribeOnUnbindView(
                RxPaparazzo.takeImage(target)
                        .crop()
                        .usingCamera()
                        .subscribe(response -> {
                            if (response.resultCode() != Activity.RESULT_OK) {
                                executeIfViewBound(NewPostView::showGalleryError);
                                return;
                            }

                            PlainImage image = new PlainImage(response.data(), new Point(0, 0));
                            attachments.add(image);

                            response.targetUI().showNewAttachment(image);
                        })
        );
    }

    public <T extends Fragment & NewPostView> void loadImageFromGallery(@NonNull T target) {
        unsubscribeOnUnbindView(
                RxPaparazzo.takeImage(target)
                        .useInternalStorage()
                        .usingGallery()
                        .subscribe(response -> {
                            if (response.resultCode() != Activity.RESULT_OK) {
                                executeIfViewBound(NewPostView::showGalleryError);
                                return;
                            }
                            PlainImage image = new PlainImage(Uri.parse(response.data()).getPath(),
                                    new Point(0, 0));
                            attachments.add(image);

                            response.targetUI().showNewAttachment(image);
                        })
        );
    }

    public void uploadPost(@Nullable String text,
                           @NonNull List<Attachment> attachments,
                           @NonNull List<NetworkContract> networks
    ) {
        postUploadModel.uploadPost(text, ListUtils.asSolidList(attachments), networks)
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnNext(loudlyPost -> {

                    executeIfViewBound(view -> view.onPostUploadingProgress(loudlyPost));
                })
                .doOnCompleted(() -> executeIfViewBound(NewPostView::onPostUploadCompleted))
                .subscribe();
    }
}
