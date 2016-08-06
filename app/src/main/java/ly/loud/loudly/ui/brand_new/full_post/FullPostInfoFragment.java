package ly.loud.loudly.ui.brand_new.full_post;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.new_base.Comment;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.ui.PeopleListFragment;
import ly.loud.loudly.ui.brand_new.TitledFragment;
import ly.loud.loudly.ui.brand_new.adapter.FullPostInfoAdapter;
import ly.loud.loudly.ui.brand_new.adapter.FullPostInfoAdapter.FullPostInfoClickListener;
import ly.loud.loudly.util.Utils;
import solid.collections.SolidList;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;
import static ly.loud.loudly.util.ListUtils.asArrayList;

@FragmentWithArgs
public class FullPostInfoFragment extends TitledFragment<FullPostInfoView, FullPostInfoPresenter>
        implements FullPostInfoView, FullPostInfoClickListener  {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.full_post_info_layout_recycler)
    @NonNull
    RecyclerView recyclerView;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    GetterModel getterModel;

    @SuppressWarnings("NullableProblems") // onViewCreated
    @NonNull
    private FullPostInfoAdapter fullPostInfoAdapter;

    @SuppressWarnings("NullableProblems") // Arg
    @Arg
    @NonNull
    PlainPost post;

    public static FullPostInfoFragment newInstance(@NonNull PlainPost post) {
        return new FullPostInfoFragmentBuilder(post).build();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);
        FragmentArgs.inject(this);
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_post_info_layout, container, false);
    }

    @Override
    @NonNull
    public FullPostInfoPresenter createPresenter() {
        return new FullPostInfoPresenter(getterModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        fullPostInfoAdapter = new FullPostInfoAdapter(post);
        fullPostInfoAdapter.setFullPostInfoClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(fullPostInfoAdapter);
        presenter.loadComments(post);
    }

    @Override
    @NonNull
    public String getTitle() {
        return String.format(
                getString(R.string.full_post_info_title),
                getString(Utils.getNetworkTitleResourceByPost(post))
        );
    }

    @Override
    public void onNewCommentsFromNetwork(@NonNull SolidList<Comment> comments, @Networks.Network int network) {
        fullPostInfoAdapter.addComments(comments, network);
    }

    @Override
    public void onError(@StringRes int errorRes) {
        Toast.makeText(getContext(), errorRes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostSharesClick(@NonNull PlainPost post) {
        PeopleListFragment fragment = PeopleListFragment.newInstance(
                Utils.getInstances(post),
                SHARES
        );
        fragment.show(getFragmentManager(), fragment.getTag());
    }

    @Override
    public void onPostLikesClick(@NonNull PlainPost post) {
        PeopleListFragment fragment = PeopleListFragment.newInstance(
                Utils.getInstances(post),
                LIKES
        );
        fragment.show(getFragmentManager(), fragment.getTag());
    }

    @Override
    public void onCommentLikesClick(@NonNull Comment comment) {
        PeopleListFragment fragment = PeopleListFragment.newInstance(
                asArrayList(comment),
                LIKES
        );
        fragment.show(getFragmentManager(), fragment.getTag());
    }
}
