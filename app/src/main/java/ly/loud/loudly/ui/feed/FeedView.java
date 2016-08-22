package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.base.plain.PlainPost;
import solid.collections.SolidList;

@UiThread
public interface FeedView extends MvpView {

    /**
     * Invokes when presenter ensured that download possible and start updating posts.
     */
    void onPostsUpdateStarted();

    void onPostsUpdated(@NonNull SolidList<PlainPost> posts);

    void onAllPostsLoaded();

    void onNetworkProblems();

    void onNoConnectedNetworksDetected();
}
