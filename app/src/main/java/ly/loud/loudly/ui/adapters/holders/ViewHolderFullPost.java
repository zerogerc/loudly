package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
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

public class ViewHolderFullPost extends BindingViewHolder<PlainPost> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_social_icon)
    @NonNull
    ImageView postIconView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_date)
    @NonNull
    TextView dateTextView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_geo)
    @NonNull
    TextView geoTextView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_text)
    @NonNull
    TextView postTextView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_image)
    @NonNull
    GlideImageView postImageView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_shares)
    @NonNull
    TextView sharesView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.list_item_full_post_likes)
    @NonNull
    TextView likesView;

    @Nullable
    private ViewHolderFullPostClickListener clickListener;

    @NonNull
    public static ViewHolderFullPost provideNewHolderWithListener(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent,
            @NonNull ViewHolderFullPostClickListener listener
    ) {
        final ViewHolderFullPost viewHolderPost = new ViewHolderFullPost(inflater, parent);
        viewHolderPost.setClickListener(listener);
        return viewHolderPost;
    }


    public ViewHolderFullPost(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_full_post, parent, false));
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@NonNull PlainPost post) {
        initHeader(post);
        initBody(post);
        initFooter(post);
    }


    @OnClick(R.id.list_item_full_post_shares)
    public void onSharesClick() {
        if (clickListener != null) {
            clickListener.onSharesClick(getAdapterPosition());
        }
    }

    @OnClick(R.id.list_item_full_post_likes)
    public void onLikesClick() {
        if (clickListener != null) {
            clickListener.onLikesClick(getAdapterPosition());
        }
    }

    private void initHeader(@NonNull PlainPost post) {
        postIconView.setImageResource(Utils.getResourceByPost(post));
        dateTextView.setText(Utils.getDateFormatted(post.getDate()));
    }

    private void initBody(@NonNull PlainPost post) {
        postTextView.setText(post.getText());

        ArrayList attachments = post.getAttachments();
        if (!attachments.isEmpty() && attachments.get(0) instanceof PlainImage) {
            PlainImage image = (PlainImage) attachments.get(0);
            postImageView.loadImage(image);
        }
    }

    private void initFooter(@NonNull PlainPost post) {
        // TODO: remove duplication
        if (post instanceof ElementWithInfo) {
            ElementWithInfo element = (ElementWithInfo) post;
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

    public void setClickListener(@Nullable ViewHolderFullPostClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ViewHolderFullPostClickListener {
        void onSharesClick(int position);

        void onLikesClick(int position);
    }
}
