package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.ui.views.GlideImageView;
import ly.loud.loudly.util.Utils;

import static android.view.View.GONE;

public class ViewHolderComment extends BindingViewHolder<Comment> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_comment_icon)
    @NonNull
    GlideImageView iconView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_comment_title)
    @NonNull
    TextView titleView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_comment_text)
    @NonNull
    TextView textView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_comment_date)
    @NonNull
    TextView dateView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_comment_likes)
    @NonNull
    TextView likesView;

    @Nullable
    private ViewHolderCommentClickListener clickListener;

    @NonNull
    public static ViewHolderComment provideNewHolderWithListener(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent,
            @NonNull ViewHolderCommentClickListener listener
    ) {
        final ViewHolderComment viewHolderPost = new ViewHolderComment(inflater, parent);
        viewHolderPost.setClickListener(listener);
        return viewHolderPost;
    }

    public ViewHolderComment(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_comment, parent, false));

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@NonNull Comment comment) {
        if (comment.getPerson().getPhotoUrl() != null) {
            iconView.loadCircularShapeImageByUrl(comment.getPerson().getPhotoUrl());
        } else {
            iconView.setImageResource(R.drawable.ic_loudly_color);
        }
        Utils.loadName(comment.getPerson(), titleView);

        textView.setText(comment.getText());

        dateView.setText(Utils.getDateFormatted(comment.getDate()));

        if (comment.getInfo().like == 0) {
            likesView.setVisibility(GONE);
        } else {
            likesView.setText(String.valueOf(comment.getInfo().like));
        }
    }

    public void setClickListener(@NonNull ViewHolderCommentClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @OnClick(R.id.list_item_comment_likes)
    public void onLikesClick() {
        if (clickListener != null) {
            clickListener.onLikesClick(getAdapterPosition());
        }
    }

    @OnClick(R.id.list_item_comment_icon)
    public void onPhotoClick() {
        if (clickListener != null) {
            clickListener.onPhotoClick(getAdapterPosition());
        }
    }

    @OnClick(R.id.list_item_comment_layout)
    public void onCommentClick() {
        if (clickListener != null) {
            clickListener.onCommentClick(getAdapterPosition());
        }
    }

    public interface ViewHolderCommentClickListener {
        void onLikesClick(int position);

        void onPhotoClick(int position);

        void onCommentClick(int position);
    }
}
