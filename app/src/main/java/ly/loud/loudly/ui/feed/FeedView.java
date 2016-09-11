package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.base.plain.PlainPost;
import solid.collections.SolidList;

@UiThread
public interface FeedView extends MvpView {

    /**
     * Callback of get cached posts. Invokes only once when posts from all networks loaded from cache.
     */
    void onCachedPostsReceived(@NonNull SolidList<PlainPost> posts);

    /**
     * Callback of initial load of posts. First list in stream contains
     * posts from DB, next contains previous list merged with posts from some new network
     */
    void onInitialLoadProgress(@NonNull SolidList<PlainPost> posts);

    /**
     * Indicates that initial load finished.
     */
    void onInitialLoadFinished();

    /**
     * Callback of refresh posts. It called once with posts from all networks.
     */
    void onPostsRefreshed(@NonNull SolidList<PlainPost> posts);

    /**
     * Callback of load more posts. Invokes once when posts from all networks loaded.
     */
    void onLoadMorePosts(@NonNull SolidList<PlainPost> posts);

    /**
     * Callback of update posts. Does not change the posts amount but changes posts info
     * (e.g likes, shares, comments)
     */
    void onPostsUpdated(@NonNull SolidList<PlainPost> posts);

    /**
     * Callback that indicates that no more posts could be loaded from networks.
     * Due to user doesn't have it on his account.
     * If user don't have connected accounts when this method will not be invoked.
     */
    void onAllPostsLoaded();

    /**
     * Indicate any error while loading posts from network.
     */
    void onNetworkProblems();

    /**
     * Indicates that user have no connected networks.
     * Could been invoked even if user have account connected but have invalid access token.
     * (e.g. outdated)
     */
    void onNoConnectedNetworksDetected();
}
