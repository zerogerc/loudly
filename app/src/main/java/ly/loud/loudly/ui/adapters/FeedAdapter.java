package ly.loud.loudly.ui.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.adapters.holders.ViewHolderPost;
import ly.loud.loudly.ui.adapters.holders.ViewHolderPost.ViewHolderPostClickListener;
import solid.collections.SolidList;

public class FeedAdapter extends RecyclerView.Adapter<ViewHolderPost>
        implements ViewHolderPostClickListener {

    @NonNull
    private SolidList<PlainPost> posts;

    @NonNull
    private final PostClickListener listener;

    @Nullable
    private NeedMoreItemsCallback needMoreItemsCallback;

    public FeedAdapter(@NonNull PostClickListener listener) {
        this.listener = listener;
        this.posts = SolidList.empty();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        PlainPost post = posts.get(position);
        return post.getDate();
    }

    @Override
    public ViewHolderPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderPost.provideNewHolderWithListener(
                LayoutInflater.from(parent.getContext()),
                parent,
                this
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPost holder, int position) {
        if (position == posts.size() - 1 && needMoreItemsCallback != null) {
            needMoreItemsCallback.needMoreItems();
        }
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(@NonNull SolidList<PlainPost> newPosts) {
        posts = newPosts;
        notifyDataSetChanged();
    }

    public void updatePosts(@NonNull SolidList<PlainPost> newPosts) {
        setPosts(newPosts);
    }

    @Override
    public void onFullPostClick(int position) {
        listener.onFullPostClick(posts.get(position));
    }

    @Override
    public void onSharesClick(int position) {
        listener.onSharesClick(posts.get(position));
    }

    @Override
    public void onLikesClick(int position) {
        listener.onLikesClick(posts.get(position));
    }

    @Override
    public void onDeleteClick(int position) {
        listener.onDeleteClick(posts.get(position));
    }

    public void setNeedMoreItemsCallback(@Nullable NeedMoreItemsCallback needMoreItemsCallback) {
        this.needMoreItemsCallback = needMoreItemsCallback;
    }

    public interface PostClickListener {
        void onFullPostClick(@NonNull PlainPost post);

        void onSharesClick(@NonNull PlainPost post);

        void onLikesClick(@NonNull PlainPost post);

        void onDeleteClick(@NonNull PlainPost post);
    }

    public interface NeedMoreItemsCallback {
        void needMoreItems();
    }
}
