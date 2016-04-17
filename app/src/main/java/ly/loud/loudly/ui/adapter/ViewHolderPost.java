package ly.loud.loudly.ui.adapter;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.ui.Loudly;
import ly.loud.loudly.util.Utils;

/**
 * Created by ZeRoGerc on 18.02.16.
 * ITMO University
 */

public class ViewHolderPost extends ViewHolder<Post> {
    private ImageView socialIcon;
    private TextView text;
    private TextView data;
    private TextView geoData;
    private TextView commentsAmount;
    private ImageView commentsButton;
    private TextView likesAmount;
    private ImageView likesButton;
    private TextView sharesAmount;
    private ImageView repostsButton;
    private ImageView postImageView;
    private ImageView showMoreOptions;

    public ViewHolderPost(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.list_post, parent, false));
        
        Post post = new Post();

        socialIcon = (ImageView) itemView.findViewById(R.id.post_view_social_network_icon);
        text = (TextView) itemView.findViewById(R.id.post_view_post_text);
        data = (TextView) itemView.findViewById(R.id.post_view_data_text);
        geoData = (TextView) itemView.findViewById(R.id.post_view_geo_data_text);
        commentsAmount = (TextView) itemView.findViewById(R.id.post_view_comments_amount);
        commentsButton = (ImageView) itemView.findViewById(R.id.post_view_comments_button);
        likesAmount = (TextView) itemView.findViewById(R.id.post_view_likes_amount);
        likesButton = (ImageView) itemView.findViewById(R.id.post_view_likes_button);
        sharesAmount = (TextView) itemView.findViewById(R.id.post_view_reposts_amount);
        repostsButton = (ImageView) itemView.findViewById(R.id.post_view_reposts_button);
        postImageView = (ImageView) itemView.findViewById(R.id.post_view_post_image);
        showMoreOptions = (ImageView) itemView.findViewById(R.id.post_view_more_options_button);

        geoData.setHeight(0);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) geoData.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        geoData.setLayoutParams(params);
        refresh(post);
    }

    @Override
    public void refresh(final Post post) {
        text.setText(post.getText());
//        data.setText(Utils.getDateFormatted(post.getDate()));
        data.setText(Long.toString(post.getDate()));
        loadPictures(post);
        handleButtons(post);
    }

    private void loadPictures(final Post post) {
        int resource = Utils.getResourceByNetwork(post.getNetwork());
        Glide.with(Loudly.getContext()).load("image")
                .error(resource)
                .placeholder(resource)
                .into(socialIcon);


        if (post.getAttachments().size() != 0) {
            postImageView.setImageBitmap(null);

            final Image image = (Image) post.getAttachments().get(0);
            resizeImageView(post);

            Glide.with(Loudly.getContext()).load(image.getUri())
                    .override(Utils.getDefaultScreenWidth(), Utils.getDefaultScreenHeight())
                    .fitCenter()
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.e("GLIDE", "Error occured during image load", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            image.setWidth(resource.getIntrinsicWidth());
                            image.setHeight(resource.getIntrinsicHeight());
                            return false;
                        }
                    })
                    .into(postImageView);
        } else {
            postImageView.setImageBitmap(null);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
            postImageView.setLayoutParams(layoutParams);
        }
    }

    private void handleButtons(final Post post) {
        int like, comment, shares;
        Info info = (post instanceof LoudlyPost) ? ((LoudlyPost) post).getInfo(Networks.LOUDLY) :
                post.getInfo();

        if (info != null) {
            like = info.like;
            comment = info.comment;
            shares = info.repost;
        } else {
            like = 0;
            comment = 0;
            shares = 0;
        }
        if (comment != 0) {
            commentsAmount.setText(Integer.toString(comment));
            setOverShadowFooterComponent(commentsButton, commentsAmount, true);

        } else {
            setOverShadowFooterComponent(commentsButton, commentsAmount, false);
            setCommentsOnClick(null);
        }

        if (like != 0) {
            likesAmount.setText(Integer.toString(like));
            setOverShadowFooterComponent(likesButton, likesAmount, true);
        } else {
            setOverShadowFooterComponent(likesButton, likesAmount, false);
            setLikesOnClick(null);
        }

        if (shares != 0) {
            sharesAmount.setText(Integer.toString(shares));
            setOverShadowFooterComponent(repostsButton, sharesAmount, true);
        } else {
            setOverShadowFooterComponent(repostsButton, sharesAmount, false);
            setRepostsOnClick(null);
        }
    }

    private void setOverShadowFooterComponent(ImageView image, TextView text, boolean flag) {
        if (flag) {
            text.setVisibility(View.VISIBLE);
            image.setColorFilter(ContextCompat.getColor(Loudly.getContext(), R.color.colorAccent));
        } else {
            text.setVisibility(View.INVISIBLE);
            image.setColorFilter(Loudly.getContext().getResources().getColor(R.color.light_grey_color));
        }
    }

    private void resizeImageView(final Post post) {
        if (post.getAttachments().size() != 0) {
            if (post.getAttachments().get(0) instanceof Image) {
                Image image = ((Image) post.getAttachments().get(0));
                int width = image.getWidth();
                int height = image.getHeight();

                if (width != 0 && height != 0) {
                    setViewSizesForImageSizes(image.getWidth(), image.getHeight());
                } else {
                    postImageView.setImageBitmap(null);
                    FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) postImageView.getLayoutParams());
                    lp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    lp.width = FrameLayout.LayoutParams.WRAP_CONTENT;
                    postImageView.setLayoutParams(lp);
                }
            }
        } else {
            postImageView.setImageBitmap(null);
            postImageView.getLayoutParams().width = 0;
            postImageView.getLayoutParams().height = 0;
            postImageView.requestLayout();
        }
    }

    private void setViewSizesForImageSizes(final int width, final int height) {
        int imageWidth = Utils.getDefaultScreenWidth();
        //TODO: remove
        if (Loudly.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageWidth= Utils.getDefaultScreenHeight() / 2;
        }
        float scale = ((float) imageWidth) / ((float) width);
        int imageHeight = (int) (height * scale);
        postImageView.getLayoutParams().width = imageWidth;
        postImageView.getLayoutParams().height = imageHeight;
        postImageView.requestLayout();
    }

    public void setLikesOnClick(View.OnClickListener listener) {
        likesButton.setOnClickListener(listener);
    }

    public void setRepostsOnClick(View.OnClickListener listener) {
        repostsButton.setOnClickListener(listener);
    }

    public void setCommentsOnClick(View.OnClickListener listener) {
        commentsButton.setOnClickListener(listener);
    }

    public void setDeleteOnClick(View.OnClickListener listener) {
        showMoreOptions.setOnClickListener(listener);
    }
}
