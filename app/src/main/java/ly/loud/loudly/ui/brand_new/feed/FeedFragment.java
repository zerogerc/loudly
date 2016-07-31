package ly.loud.loudly.ui.brand_new.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.viewstate.MvpViewStateFragment;
import com.hannesdorfmann.mosby.mvp.viewstate.ViewState;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.ui.PostsAdapter;
import ly.loud.loudly.ui.brand_new.views.FeedRecyclerView;

public class FeedFragment extends MvpViewStateFragment<FeedView, FeedPresenter>
    implements FeedView {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.feed_recycler_view)
    @NonNull
    FeedRecyclerView feedRecyclerView;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    Loudly loudlyApp;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    PostLoadModel postLoadModel;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    GetterModel getterModel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private PostsAdapter postsAdapter;

    @NonNull
    private final List<PlainPost> postsList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_feed, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        presenter.loadPosts();
    }

    @Override
    public void onDestroyView() {
        presenter.unsubscribeAll();
        super.onDestroyView();
    }

    @Override
    public void onNewLoadedPosts(@NonNull List<PlainPost> posts) {
        postsList.addAll(posts);
        postsAdapter.notifyDataSetChanged();
    }

    @Override
    @NonNull
    public ViewState createViewState() {
        return new FeedViewState();
    }

    @Override
    public void onNewViewStateInstance() {

    }

    @Override
    @NonNull
    public FeedPresenter createPresenter() {
        return new FeedPresenter(
                loudlyApp,
                postLoadModel,
                getterModel
        );
    }

    private void initRecyclerView() {
        postsAdapter = new PostsAdapter(postsList, (AppCompatActivity) getActivity());
        Loudly.getPostHolder().setAdapter(postsAdapter);
        feedRecyclerView.setAdapter(postsAdapter);
    }
}
