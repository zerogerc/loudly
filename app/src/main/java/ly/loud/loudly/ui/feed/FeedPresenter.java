package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.BasePresenter;
import rx.Subscription;
import rx.functions.Action1;
import solid.collections.SolidList;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

// TODO: config
public class FeedPresenter extends BasePresenter<FeedView> {

    @NonNull
    private Loudly loudlyApp;

    @NonNull
    private CoreModel coreModel;

    @NonNull
    private GetterModel getterModel;

    @NonNull
    private PostDeleterModel deleterModel;

    @NonNull
    private PostLoadModel postLoadModel;

    @NonNull
    private LoadMoreStrategyModel loadMoreStrategyModel;

    @Nullable
    private Subscription loader;

    public FeedPresenter(
            @NonNull Loudly loudlyApp,
            @NonNull CoreModel coreModel,
            @NonNull PostLoadModel postLoadModel,
            @NonNull GetterModel getterModel,
            @NonNull PostDeleterModel deleterModel,
            @NonNull LoadMoreStrategyModel loadMoreStrategyModel
    ) {
        this.loudlyApp = loudlyApp;
        this.coreModel = coreModel;
        this.postLoadModel = postLoadModel;
        this.getterModel = getterModel;
        this.deleterModel = deleterModel;
        this.loadMoreStrategyModel = loadMoreStrategyModel;
    }

    public SolidList<PlainPost> getCachedPosts() {
        return postLoadModel.getCachedPosts();
    }

    public void updateMorePosts() {
        if (loadMoreStrategyModel.isAllPostsLoaded()) {
            executeIfViewBound(FeedView::onAllPostsLoaded);
            return;
        }

        int sizePrevious = postLoadModel.getCachedPosts().size();
        loadMoreStrategyModel.generateNextInterval();

        loadPosts(result -> executeIfViewBound(view -> {
            if (result.size() != sizePrevious) {
                view.onPostsUpdated(result);
            } else {
                // load more items if no posts loaded
                updateMorePosts();
            }
        }));
    }

    public void startUpdatePosts() {
        if (!isAnyNetworkConnected()) {
            executeIfViewBound(FeedView::onNoConnectedNetworksDetected);
            return;
        }

        executeIfViewBound(FeedView::onPostsUpdateStarted);
        loadPosts(result -> executeIfViewBound(view -> view.onPostsUpdated(result)));
    }

    public void unsubscribeAll() {
    }

    public void deletePost(@NonNull LoudlyPost post) {
        deleterModel.deletePostFromAllNetworks(post)
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnCompleted(this::startUpdatePosts)
                .subscribe();
    }

    private boolean isAnyNetworkConnected() {
        return coreModel.getConnectedNetworksModels().size() > 0;
    }

    private void loadPosts(@NonNull Action1<SolidList<PlainPost>> resultAction) {
        if (loader != null && !loader.isUnsubscribed()) {
            loader.unsubscribe();
        }

        loader = postLoadModel.getPostsByInterval(loadMoreStrategyModel.getCurrentTimeInterval())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(
                        resultAction,
                        error -> {
                            Log.e("tag", "error", error);
                            executeIfViewBound(FeedView::onNetworkProblems);
                        }
                );
    }

}
