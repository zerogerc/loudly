package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import ly.loud.loudly.base.plain.PlainPost;

@UiThread
public interface FeedView extends MvpView {
    void onNewLoadedPosts(@NonNull List<? extends PlainPost> posts);
}
