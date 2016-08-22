package ly.loud.loudly.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.adapters.holders.BindingViewHolder;
import ly.loud.loudly.ui.adapters.holders.ItemTypes;
import ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import ly.loud.loudly.ui.adapters.holders.ViewHolderFactory;
import ly.loud.loudly.ui.adapters.holders.ViewHolderPost;
import ly.loud.loudly.ui.adapters.holders.ViewHolderPost.ViewHolderPostClickListener;
import solid.collections.SolidList;

public class FeedAdapter extends RecyclerView.Adapter<BindingViewHolder>
        implements ViewHolderPostClickListener {

    @NonNull
    private SolidList<PlainPost> posts;

    @NonNull
    private final PostClickListener listener;

    private boolean needLoadMore = true;

    public FeedAdapter(@NonNull PostClickListener listener) {
        this.listener = listener;
        this.posts = SolidList.empty();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (isLoadMoreItem(position)) {
            return -1;
        } else {
            PlainPost post = posts.get(position);
            return post.getDate();
        }
    }

    @Override
    @ItemType
    public int getItemViewType(int position) {
        if (isLoadMoreItem(position)) {
            return ItemTypes.LOAD_MORE;
        } else {
            return ItemTypes.POST;
        }
    }

    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ItemType int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ItemTypes.LOAD_MORE:
                return ViewHolderFactory.provideViewHolder(layoutInflater, parent, viewType);
            default:
                // need to set listener and for consistent initialization don't use factory here
                return ViewHolderPost.provideNewHolderWithListener(layoutInflater, parent, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        if (isLoadMoreItem(position)) {
            //noinspection ConstantConditions (ViewHolderLoadMore don't need ListItem)
            holder.bind(null);
            StaggeredGridLayoutManager.LayoutParams layoutParams =
                    (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            ((ViewHolderPost) holder).bind(posts.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (needLoadMore) {
            return posts.size() + 1;
        }
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

    public void setNoLoadMore() {
        boolean old = needLoadMore;
        needLoadMore = false;
        if (old) {
            notifyItemRemoved(posts.size());
        }
    }

    /**
     * Check if this position is the position with progress bar.
     */
    private boolean isLoadMoreItem(int position) {
        return needLoadMore && getItemCount() - 1 == position;
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
