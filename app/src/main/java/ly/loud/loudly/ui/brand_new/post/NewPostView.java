package ly.loud.loudly.ui.brand_new.post;

import android.support.annotation.Nullable;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.base.attachments.Image;

public interface NewPostView extends MvpView {
    void onImageChoosen(@Nullable Image image);
}
