package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    private volatile boolean isInitialLoadInProgress = false;

    private volatile boolean isLoadMoreInProgress = false;

    @Nullable
    private Subscription loader;

    @Nullable
    private Subscription updater;

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

    public void loadCachedPosts() {
        executeIfViewBound(view -> view.onCachedPostsReceived(postLoadModel.getCachedPosts()));
    }

    public void initialLoad() {
        if (!isAnyNetworkConnected()) {
            executeIfViewBound(FeedView::onNoConnectedNetworksDetected);
            return;
        }

        isInitialLoadInProgress = true;
        postLoadModel.loadPosts(loadMoreStrategyModel.getCurrentTimeInterval())
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnNext(posts -> executeIfViewBound(view -> view.onInitialLoadProgress(posts)))
                .doOnCompleted(() -> {
                    isInitialLoadInProgress = false;
                    executeIfViewBound(FeedView::onInitialLoadFinished);
                    subscribeOnUpdates();
                })
                .doOnError(error -> executeIfViewBound(FeedView::onNetworkProblems))
                .subscribe(
                        result -> {},
                        error -> isInitialLoadInProgress = false
                );
    }

    public void refreshPosts() {
        if (!isAnyNetworkConnected()) {
            executeIfViewBound(FeedView::onNoConnectedNetworksDetected);
            return;
        }

        if (isLoadMoreInProgress || isInitialLoadInProgress) {
            return;
        }

        loadPosts(posts -> executeIfViewBound(view -> view.onPostsRefreshed(posts)));
    }

    public void loadMorePosts() {
        if (loadMoreStrategyModel.isAllPostsLoaded()) {
            executeIfViewBound(FeedView::onAllPostsLoaded);
            return;
        }

        /**
         * View invokes loadMore through LayoutManager. It could invokes it really often.
         * It's better to add some sync here.
         */
        if (isLoadMoreInProgress || isInitialLoadInProgress) {
            return;
        } else {
            isLoadMoreInProgress = true;
        }

        loadMoreStrategyModel.generateNextInterval();
        loadPosts(result -> {
            isLoadMoreInProgress = false;
            executeIfViewBound(view -> view.onLoadMorePosts(result));
        });
    }


    public void deletePost(@NonNull LoudlyPost post) {
        deleterModel.deletePostFromAllNetworks(post)
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnCompleted(this::refreshPosts)
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
                        posts -> {
                            resultAction.call(posts);
                            subscribeOnUpdates();
                        },
                        error -> executeIfViewBound(FeedView::onNetworkProblems)
                );
    }

    private void subscribeOnUpdates() {
        if (updater != null && !updater.isUnsubscribed()) {
            updater.unsubscribe();
        }
        updater = postLoadModel.observeUpdatedList()
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(updatedPosts -> {
                    executeIfViewBound(view -> view.onPostsUpdated(updatedPosts));
                });
    }

}
