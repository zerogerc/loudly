package ly.loud.loudly.ui.brand_new;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import ly.loud.loudly.base.says.Post;

@UiThread
public interface FeedView extends MvpView {
    void onNewLoadedPosts(@NonNull List<Post> posts);
}
