package ly.loud.loudly.ui.feed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import icepick.State;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.exceptions.TokenExpiredException;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.util.BasePresenter;
import ly.loud.loudly.util.RxUtils;
import rx.Subscription;
import rx.functions.Action1;
import solid.collections.SolidList;

import static ly.loud.loudly.networks.Networks.LOUDLY;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;
import static solid.collectors.ToSolidList.toSolidList;

// TODO: config
public class FeedPresenter extends BasePresenter<FeedView> {

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

    @State
    volatile boolean filterPosts = false;

    @State
    @Network
    volatile int filterPostsByNetwork = LOUDLY;

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
        super(loudlyApp);
        this.coreModel = coreModel;
        this.postLoadModel = postLoadModel;
        this.getterModel = getterModel;
        this.deleterModel = deleterModel;
        this.loadMoreStrategyModel = loadMoreStrategyModel;
    }

    public void clearFilter() {
        filterPosts = false;
        executeIfViewBound(view -> view.shouldChangeTitle(R.string.feed));
        loadCachedPosts();
    }

    public void filterPostsByNetwork(@Network int network) {
        filterPosts = true;
        filterPostsByNetwork = network;
        executeIfViewBound(view -> view.shouldChangeTitle(Networks.nameResourceOfNetwork(network)));
        loadCachedPosts();
    }

    @NonNull
    private SolidList<PlainPost> filterPosts(@NonNull SolidList<PlainPost> posts) {
        if (filterPosts) {
            return posts
                    .map(post -> {
                        if (post instanceof SinglePost) {
                            SinglePost singlePost = (SinglePost) post;
                            return singlePost.getNetwork() == filterPostsByNetwork ?
                                    post : null;
                        }
                        if (post instanceof LoudlyPost) {
                            if (filterPostsByNetwork == LOUDLY) {
                                return post;
                            }
                            return ((LoudlyPost) post)
                                    .getSingleNetworkInstance(filterPostsByNetwork);
                        }
                        return null;
                    })
                    .filter(RxUtils::nonNull)
                    .collect(toSolidList());
        } else {
            return posts;
        }
    }

    @Override
    public void onBindView(@NonNull FeedView view) {
        super.onBindView(view);
        executeIfViewBound(v -> v.onTokenExpiredException(Networks.OK));

        unsubscribeOnUnbindView(
                postLoadModel.observeLoadErrors()
                        .subscribeOn(io())
                        .observeOn(mainThread())
                        .subscribe(error -> {
                            if (error instanceof TokenExpiredException) {
                                executeIfViewBound(v -> v.onTokenExpiredException(((TokenExpiredException) error).network));
                            } else {
                                executeIfViewBound(FeedView::onNetworkProblems);
                            }
                        })
        );
    }

    public void loadCachedPosts() {
        executeIfViewBound(view -> view.onCachedPostsReceived(
                filterPosts(postLoadModel.getCachedPosts()))
        );
    }

    public void initialLoad() {
        if (!isAnyNetworkConnected()) {
            executeIfViewBound(FeedView::onNoConnectedNetworksDetected);
            return;
        }
        if (loader != null && !loader.isUnsubscribed()) {
            loader.unsubscribe();
        }

        isInitialLoadInProgress = true;
        loader = postLoadModel
                .loadPosts(loadMoreStrategyModel.getCurrentTimeInterval())
                .map(this::filterPosts)
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnNext(posts -> executeIfViewBound(view -> view.onInitialLoadProgress(posts)))
                .doOnCompleted(() -> {
                    isInitialLoadInProgress = false;
                    executeIfViewBound(FeedView::onInitialLoadFinished);
                    subscribeOnUpdates();
                })
                .subscribe(
                        result -> {},
                        error -> {
                            Log.wtf("FEED_PRESENTER", "Error to UI while loading posts");
                            isInitialLoadInProgress = false;
                        }
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

        loader = postLoadModel
                .getPostsByInterval(loadMoreStrategyModel.getCurrentTimeInterval())
                .map(this::filterPosts)
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
        updater = postLoadModel
                .observeUpdatedList()
                .map(this::filterPosts)
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(updatedPosts -> {
                    executeIfViewBound(view -> view.onPostsUpdated(updatedPosts));
                });
    }

}
