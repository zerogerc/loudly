package ly.loud.loudly.ui.feed;

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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.LoadMoreStrategyModel;
import ly.loud.loudly.application.models.PostDeleterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.TitledFragment;
import ly.loud.loudly.ui.adapters.FeedAdapter;
import ly.loud.loudly.ui.full_post.FullPostInfoActivity;
import ly.loud.loudly.ui.people_list.PeopleListFragment;
import ly.loud.loudly.ui.views.FeedRecyclerView;
import ly.loud.loudly.util.Utils;
import solid.collections.SolidList;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;

public class FeedFragment extends TitledFragment<FeedView, FeedPresenter>
        implements FeedView, FeedAdapter.PostClickListener {

    private static final int MAX_SPAN_NUMBER = 2;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.content_feed_swipe_refresh_layout)
    @NonNull
    SwipeRefreshLayout refreshLayout;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.content_feed_recycler_view)
    @NonNull
    FeedRecyclerView feedRecyclerView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.content_feed_message_view)
    @NonNull
    TextView messageView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.content_feed_message_view_container)
    @NonNull
    View messageViewContainer;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    Loudly loudlyApp;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    CoreModel coreModel;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    PostLoadModel postLoadModel;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    GetterModel getterModel;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    PostDeleterModel deleterModel;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    LoadMoreStrategyModel loadMoreStrategyModel;

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private FeedAdapter adapter;

    private boolean isAllPostsLoadedInfoShowedToUser = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_feed, container, false);
    }

    @Override
    @NonNull
    public String getTitle() {
        return getString(R.string.loudly);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.accent),
                ContextCompat.getColor(getContext(), R.color.primary)
        );
        refreshLayout.setOnRefreshListener(this::updatePosts);

        adapter = new FeedAdapter(this);
        adapter.setPosts(presenter.getCachedPosts());
        feedRecyclerView.setAdapter(adapter);
        setLoadMoreListener();
    }

    @Override
    public void onDestroyView() {
        presenter.unsubscribeAll();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePosts();
    }

    public void updatePosts() {
        presenter.startUpdatePosts();
    }

    @Override
    public void onPostsUpdateStarted() {
        showContent();
    }

    @Override
    public void onPostsUpdated(@NonNull SolidList<PlainPost> posts) {
        if (posts.size() == 0) {
            // if we don't have posts we should try to load more
            presenter.updateMorePosts();
        } else {
            adapter.updatePosts(posts);
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onAllPostsLoaded() {
        adapter.setNoLoadMore();
        if (!isAllPostsLoadedInfoShowedToUser) { // show message only once
            if (getView() != null) {
                Snackbar.make(getView(), R.string.message_no_more_load_more, LENGTH_SHORT).show();
            }
        }
        isAllPostsLoadedInfoShowedToUser = true;
    }

    @Override
    public void onNetworkProblems() {
        if (adapter.getPostsCount() == 0) { // if no posts already loaded than show this message
            showMessage(R.string.message_network_problems);
            if (refreshLayout.isRefreshing()) { // if we try to connect by get error again
                refreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onNoConnectedNetworksDetected() {
        showMessage(R.string.message_no_connected_networks);
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    @NonNull
    public FeedPresenter createPresenter() {
        return new FeedPresenter(
                loudlyApp,
                coreModel,
                postLoadModel,
                getterModel,
                deleterModel,
                loadMoreStrategyModel
        );
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
        presenter.updateMorePosts();
    }

    private void showContent() {
        feedRecyclerView.setVisibility(VISIBLE);
        messageViewContainer.setVisibility(GONE);
    }

    private boolean isContentVisible() {
        return feedRecyclerView.getVisibility() == VISIBLE;
    }

    private void showMessage(@StringRes int message) {
        feedRecyclerView.setVisibility(GONE);
        messageViewContainer.setVisibility(VISIBLE);
        messageView.setText(message);
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
}