package ly.loud.loudly.ui.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.TitledFragment;
import ly.loud.loudly.ui.adapters.FeedAdapter;
import ly.loud.loudly.ui.full_post.FullPostInfoActivity;
import ly.loud.loudly.ui.people_list.PeopleListFragment;
import ly.loud.loudly.ui.views.FeedRecyclerView;
import ly.loud.loudly.util.Utils;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;

public class FeedFragment extends TitledFragment<FeedView, FeedPresenter>
        implements FeedView, FeedAdapter.PostClickListener {

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

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private FeedAdapter adapter;

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
    @NonNull
    public String getTitle() {
        return getString(R.string.loudly);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new FeedAdapter(this);
        feedRecyclerView.setAdapter(adapter);

        presenter.loadPosts();
    }

    @Override
    public void onDestroyView() {
        presenter.unsubscribeAll();
        super.onDestroyView();
    }

    @Override
    public void onNewLoadedPosts(@NonNull List<? extends PlainPost> posts) {
        adapter.setPosts(posts);
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
}