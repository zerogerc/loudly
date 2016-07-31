package ly.loud.loudly.ui.brand_new.post;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.base.attachments.Attachment;

@UiThread
public interface NewPostView extends MvpView {
    @Deprecated
    void showImage(@NonNull String url);

    void showNewAttachment(@NonNull Attachment attachment);

    void showGalleryError();
}
