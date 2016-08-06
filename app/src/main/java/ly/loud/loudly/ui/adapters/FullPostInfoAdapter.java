package ly.loud.loudly.ui.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.adapters.holders.BindingViewHolder;
import ly.loud.loudly.ui.adapters.holders.ListItem;
import ly.loud.loudly.ui.adapters.holders.ViewHolderComment;
import ly.loud.loudly.ui.adapters.holders.ViewHolderComment.ViewHolderCommentClickListener;
import ly.loud.loudly.ui.adapters.holders.ViewHolderDelimiter;
import ly.loud.loudly.ui.adapters.holders.ViewHolderFactory;
import ly.loud.loudly.ui.adapters.holders.ViewHolderFullPost;
import ly.loud.loudly.ui.adapters.holders.ViewHolderFullPost.ViewHolderFullPostClickListener;
import solid.collections.SolidList;

import static ly.loud.loudly.ui.adapters.holders.ItemTypes.COMMENT;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.POST;

public class FullPostInfoAdapter extends RecyclerView.Adapter<BindingViewHolder> {

    @NonNull
    private List<ListItem> items = new ArrayList<>();

    @NonNull
    private PlainPost post;

    @Nullable
    private FullPostInfoClickListener fullPostInfoClickListener;

    private final ViewHolderFullPostClickListener postClickListener = new ViewHolderFullPostClickListener() {
        @Override
        public void onSharesClick(int position) {
            if (fullPostInfoClickListener != null) {
                fullPostInfoClickListener.onPostSharesClick(post);
            }
        }

        @Override
        public void onLikesClick(int position) {
            if (fullPostInfoClickListener != null) {
                fullPostInfoClickListener.onPostLikesClick(post);
            }
        }
    };

    private final ViewHolderCommentClickListener commentClickListener = position -> {
        if (fullPostInfoClickListener != null && (items.get(position) instanceof Comment)) {
            fullPostInfoClickListener.onCommentLikesClick((Comment) items.get(position));
        }
    };

    public FullPostInfoAdapter(@NonNull PlainPost post) {
        this.post = post;
        items.add(post);
    }

    @Override
    @ItemType
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ItemType int viewType) {
        switch (viewType) {
            case POST:
                return ViewHolderFullPost.provideNewHolderWithListener(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        postClickListener
                );
            case COMMENT:
                return ViewHolderComment.provideNewHolderWithListener(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        commentClickListener
                );
            default:
                return ViewHolderFactory.provideViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case POST:
                ((ViewHolderFullPost) holder).bind(((PlainPost) items.get(position)));
                break;
            case COMMENT:
                ((ViewHolderComment) holder).bind(((Comment) items.get(position)));
                break;
            default: // DELIMITER
                ((ViewHolderDelimiter) holder).bind(((NetworkDelimiter) items.get(position)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setFullPostInfoClickListener(@Nullable FullPostInfoClickListener fullPostInfoClickListener) {
        this.fullPostInfoClickListener = fullPostInfoClickListener;
    }

    /**
     * Adds delimiter corresponding to the given network following by list of given comments.
     * @param comments - list of persons to add
     * @param network - network which delimiter will be inserted
     */
    public void addComments(@NonNull SolidList<Comment> comments, @Network int network) {
        int positionStart = items.size();

        if (!comments.isEmpty()) {
            items.add(new NetworkDelimiter(network));
            items.addAll(comments);
        }

        notifyItemRangeChanged(positionStart, comments.size() + 1);
    }

    public interface FullPostInfoClickListener {
        void onPostSharesClick(@NonNull PlainPost post);

        void onPostLikesClick(@NonNull PlainPost post);

        void onCommentLikesClick(@NonNull Comment comment);
    }
}
