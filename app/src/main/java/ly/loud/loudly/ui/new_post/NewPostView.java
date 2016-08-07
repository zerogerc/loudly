package ly.loud.loudly.ui.new_post;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.base.interfaces.attachments.Attachment;


@UiThread
public interface NewPostView extends MvpView {

    void showNewAttachment(@NonNull Attachment attachment);

    void showGalleryError();
}
