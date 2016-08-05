package ly.loud.loudly.ui.brand_new.feed;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.new_base.plain.PlainPost;

@UiThread
public interface FeedView extends MvpView {
    void onNewLoadedPosts(@NonNull List<? extends PlainPost> posts);
}
