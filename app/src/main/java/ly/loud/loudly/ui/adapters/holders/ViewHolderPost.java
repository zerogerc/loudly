package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.interfaces.ElementWithInfo;
import ly.loud.loudly.base.plain.PlainImage;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.ui.views.GlideImageView;
import ly.loud.loudly.util.Utils;

public class ViewHolderPost extends BindingViewHolder<PlainPost> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_social_icon)
    @NonNull
    ImageView postIconView;
    
    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_date)
    @NonNull
    TextView dateTextView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_geo)
    @NonNull
    TextView geoTextView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_show_more_button)
    @NonNull
    View showMoreView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_text)
    @NonNull
    TextView postTextView;
    
    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_image)
    @NonNull
    GlideImageView postImageView;
    
    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_comments)
    @NonNull
    TextView commentsView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_shares)
    @NonNull
    TextView sharesView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_post_likes)
    @NonNull
    TextView likesView;

    @Nullable
    private ViewHolderPostClickListener clickListener;

    @Nullable
    private PlainPost currentPost;

    @NonNull
    public static ViewHolderPost provideNewHolderWithListener(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent,
            @NonNull ViewHolderPostClickListener listener
    ) {
        final ViewHolderPost viewHolderPost = new ViewHolderPost(inflater, parent);
        viewHolderPost.setClickListener(listener);
        return viewHolderPost;
    }

    public ViewHolderPost(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_post, parent, false));
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@NonNull PlainPost post) {
        currentPost = post;
        reset();
        bindHeader(post);
        bindBody(post);
        bindFooter(post);
    }

    @Nullable
    public PlainPost getCurrentPost() {
        return currentPost;
    }

    public void setClickListener(@Nullable ViewHolderPostClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @OnClick(R.id.list_item_post_root)
    public void onFullPostClicked() {
        if (clickListener != null) {
            clickListener.onFullPostClick(getAdapterPosition());
        }
    }

    @OnClick(R.id.list_item_post_shares)
    public void  onSharesClick() {
        if (clickListener != null) {
            clickListener.onSharesClick(getAdapterPosition());
        }
    }

    @OnClick(R.id.list_item_post_likes)
    public void onLikesClick() {
        if (clickListener != null) {
            clickListener.onLikesClick(getAdapterPosition());
        }
    }

    @OnClick(R.id.list_item_post_show_more_button)
    public void onDeleteClick() {
        if (clickListener != null) {
            clickListener.onDeleteClick(getAdapterPosition());
        }
    }

    private void reset() {
        dateTextView.setText("");
        geoTextView.setText("");
        postTextView.setText("");
        postImageView.reset();

        setIcon(commentsView, R.drawable.ic_post_comment_light);
        commentsView.setText("");

        setIcon(sharesView, R.drawable.ic_post_share_light);
        sharesView.setText("");

        setIcon(likesView, R.drawable.ic_post_favourite_light);
        likesView.setText("");
    }

    private void bindHeader(@NonNull PlainPost post) {
        postIconView.setImageResource(Utils.getResourceByPost(post));
        dateTextView.setText(Utils.getDateFormatted(post.getDate()));
    }

    private void bindBody(@NonNull PlainPost post) {
        postTextView.setText(post.getText());

        ArrayList attachments = post.getAttachments();
        if (!attachments.isEmpty() && attachments.get(0) instanceof PlainImage) {
            PlainImage image = (PlainImage) attachments.get(0);
            postImageView.loadImage(image);
        }
    }

    private void bindFooter(@NonNull PlainPost post) {
        // TODO: remove duplication
        if (post instanceof ElementWithInfo) {
            ElementWithInfo element = (ElementWithInfo) post;
            setInRightState(commentsView, element.getInfo().comment, R.drawable.ic_post_comment_bright);
            setInRightState(likesView, element.getInfo().like, R.drawable.ic_post_favourite_bright);
            setInRightState(sharesView, element.getInfo().repost, R.drawable.ic_post_share_bright);
        }
    }

    private void setInRightState(@NonNull TextView icon,
                                 int amount,
                                 @DrawableRes int iconBright
    ) {
        if (amount != 0) {
            setIcon(icon, iconBright);
            icon.setText(String.valueOf(amount));
        }
    }

    private void setIcon(@NonNull TextView icon, @DrawableRes int iconRes) {
        icon.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(Loudly.getContext(), iconRes),
                null,
                null,
                null
        );
    }

    public interface ViewHolderPostClickListener {
        void onFullPostClick(int position);

        void onSharesClick(int position);

        void onLikesClick(int position);

        void onDeleteClick(int position);
    }
}