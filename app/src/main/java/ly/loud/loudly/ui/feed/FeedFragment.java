package ly.loud.loudly.ui.feed;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import icepick.Icepick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.ui.TitledFragment;
import ly.loud.loudly.ui.adapters.FeedAdapter;
import ly.loud.loudly.ui.full_post.FullPostInfoActivity;
import ly.loud.loudly.ui.people_list.PeopleListFragment;
import ly.loud.loudly.ui.views.FeedRecyclerView;
import ly.loud.loudly.util.Utils;
import solid.collections.SolidList;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static ly.loud.loudly.application.Loudly.getApplication;
import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;
import static ly.loud.loudly.util.AssertionsUtils.assertActivityImplementsInterface;

public class FeedFragment extends TitledFragment implements FeedView, FeedAdapter.PostClickListener {

    private static final int MAX_SPAN_NUMBER = 2;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.content_feed_swipe_refresh_layout)
    @NonNull
    SwipeRefreshLayout refreshLayout;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.content_feed_recycler_view)
    @NonNull
    FeedRecyclerView feedRecyclerView;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    FeedPresenter presenter;

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private FeedAdapter adapter;

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private Unbinder unbinder;

    @Override
    @NonNull
    public String getDefaultTitle() {
        return getString(R.string.feed);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplication(getContext()).getAppComponent().plus(new FeedModule()).inject(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        assertActivityImplementsInterface(getActivity(), FeedFragmentCallback.class);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((FeedFragmentCallback) getActivity()).showFeedLoading();
        unbinder = ButterKnife.bind(this, view);
        presenter.onBindView(this);
        Icepick.restoreInstanceState(presenter, savedInstanceState);
        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.accent),
                ContextCompat.getColor(getContext(), R.color.primary)
        );
        refreshLayout.setOnRefreshListener(this::refreshPosts);

        adapter = new FeedAdapter(this);

        presenter.onBindView(this);
        presenter.loadCachedPosts();
        feedRecyclerView.setAdapter(adapter);
        setLoadMoreListener();
    }

    @Override
    public void onDestroyView() {
        presenter.onUnbindView(this);
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(presenter, outState);
    }

    public void filterPostsByNetwork(@Network int network) {
        presenter.filterPostsByNetwork(network);
    }

    public void clearFilter() {
        presenter.clearFilter();
    }

    public void refreshPosts() {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
        presenter.refreshPosts();
    }

    @Override
    public void shouldChangeTitle(@StringRes int titleResource) {
        setTitle(getString(titleResource));
    }

    @Override
    public void onCachedPostsReceived(@NonNull SolidList<PlainPost> posts) {
        adapter.setPosts(posts);

        if (posts.size() != 0) {
            ((FeedFragmentCallback) getActivity()).showFeed();
            presenter.refreshPosts();
        } else {
            presenter.initialLoad();
        }
    }

    @Override
    public void onInitialLoadProgress(@NonNull SolidList<PlainPost> posts) {
        if (adapter.getPostsCount() == 0 && posts.size() > 0) {
            ((FeedFragmentCallback) getActivity()).showFeed();
        }
        adapter.setPosts(posts);
    }

    @Override
    public void onInitialLoadFinished() {
        if (adapter.getPostsCount() > 0) {
            ((FeedFragmentCallback) getActivity()).showFeed();
        } else {
            presenter.loadMorePosts();
        }
    }

    @Override
    public void onPostsRefreshed(@NonNull SolidList<PlainPost> posts) {
        adapter.setPosts(posts);
    }

    @Override
    public void onLoadMorePosts(@NonNull SolidList<PlainPost> posts) {
        int oldCount = adapter.getPostsCount();
        adapter.setPosts(posts);

        if (oldCount == 0 && posts.size() > 0) {
            ((FeedFragmentCallback) getActivity()).showFeed();
        }

        if (oldCount == posts.size()) {
            presenter.loadMorePosts();
        }
    }

    @Override
    public void onPostsUpdated(@NonNull SolidList<PlainPost> posts) {
        adapter.updatePosts(posts);
    }

    @Override
    public void onAllPostsLoaded() {
        adapter.setNoLoadMore();
    }

    @Override
    public void onNetworkProblems() {
        if (adapter.getPostsCount() == 0) { // if no posts already loaded than show this message
            ((FeedFragmentCallback) getActivity()).showFeedGlobalError(getString(R.string.message_network_problems));
        } else {
            ((FeedFragmentCallback) getActivity()).showFeedError(getString(R.string.message_network_problems));
        }
    }

    @Override
    public void onTokenExpiredException(@Network int expiredNetwork) {
        final String error = String.format(
                getString(R.string.token_expired_error),
                getString(Networks.nameResourceOfNetwork(expiredNetwork))
        );

        ((FeedFragmentCallback) getActivity()).showFeedError(error);
    }

    @Override
    public void onNoConnectedNetworksDetected() {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
        ((FeedFragmentCallback) getActivity()).showFeedGlobalError(getString(R.string.message_no_connected_networks));
    }

    @Override
    public void onFullPostClick(@NonNull PlainPost post) {
        FullPostInfoActivity.invoke(getActivity(), post);
    }

    @Override
    public void onSharesClick(@NonNull PlainPost post) {
        PeopleListFragment fragment = PeopleListFragment.newInstance(
                Utils.getInstances(post),
                SHARES
        );
        fragment.show(getFragmentManager(), fragment.getTag());
    }

    @Override
    public void onLikesClick(@NonNull PlainPost post) {
        PeopleListFragment fragment = PeopleListFragment.newInstance(
                Utils.getInstances(post),
                LIKES
        );
        fragment.show(getFragmentManager(), fragment.getTag());
    }

    @Override
    public void onDeleteClick(@NonNull PlainPost post) {
        if (post instanceof LoudlyPost) {
            showDeleteConfirmationDialog((LoudlyPost) post);
        } else {
            Toast.makeText(getContext(), R.string.only_loudly_post_could_be_deleted_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Invokes every time when view with feed detects end of list.
     */
    public void needMoreItems() {
        presenter.loadMorePosts();
    }

    /**
     * Set listener for loadMore behavior of feed.
     * Assumes that {@link FeedFragment#feedRecyclerView} already has
     * {@link android.support.v7.widget.RecyclerView.LayoutManager LayoutManager}
     */
    private void setLoadMoreListener() {
        final StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) feedRecyclerView.getLayoutManager();
        int[] cache = new int[MAX_SPAN_NUMBER];
        Arrays.fill(cache, NO_POSITION);

        feedRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                manager.findLastVisibleItemPositions(cache);
                for (int i = 0; i < MAX_SPAN_NUMBER; i++) {
                    // cache[i] - is number of post in i-th span
                    if (cache[i] == adapter.getItemCount() - 1) {
                        needMoreItems();
                    }
                }

            }
        });
    }

    private void showDefaultSnackBar(@StringRes int message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(@NonNull final LoudlyPost post) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.message_post_delete_confirmation)
                .setPositiveButton(R.string.message_confirmation_yes, (dialogInterface, i) -> {
                    showDefaultSnackBar(R.string.message_start_post_deletion);
                    presenter.deletePost(post);
                })
                .setNegativeButton(R.string.message_confirmation_no, null)
                .create()
                .show();
    }

    public interface FeedFragmentCallback {
        /**
         * Indicates that activity must show progress
         */
        void showFeedLoading();

        /**
         * Indicates fatal error while loading. Activity should hide feed and show error message.
         */
        void showFeedGlobalError(@NonNull String errorMessage);

        /**
         * Indicates error while loading. Activity should only inform user about this error.
         */
        void showFeedError(@NonNull String errorMessage);

        /**
         * Indicates that fragment load feed and already have progress view
         */
        void hideFeed();

        /**
         * Indicates that fragment have feed to show
         */
        void showFeed();
    }

    @Module
    public static class FeedModule {
        @Provides
        @NonNull
        public FeedPresenter provideFeedPresenter(
                @NonNull Loudly loudlyApp,
                @NonNull CoreModel coreModel,
                @NonNull PostLoadModel postLoadModel,
                @NonNull GetterModel getterModel,
                @NonNull PostDeleterModel deleterModel,
                @NonNull LoadMoreStrategyModel loadMoreStrategyModel
        ) {
            return new FeedPresenter(
                    loudlyApp,
                    coreModel,
                    postLoadModel,
                    getterModel,
                    deleterModel,
                    loadMoreStrategyModel
            );
        }
    }

    @Subcomponent(modules = FeedModule.class)
    public interface FeedComponent {

        void inject(@NonNull FeedFragment fragment);
    }
}