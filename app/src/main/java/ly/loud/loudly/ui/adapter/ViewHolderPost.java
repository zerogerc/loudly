package ly.loud.loudly.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.ui.FullPostInfoActivity;
import ly.loud.loudly.ui.Loudly;
import ly.loud.loudly.ui.views.GlideImageView;
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
    private GlideImageView postImageView;
    private ImageView deleteButton;

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
        postImageView = (GlideImageView) itemView.findViewById(R.id.post_view_post_image);
        deleteButton = (ImageView) itemView.findViewById(R.id.post_view_more_options_button);

        geoData.setHeight(0);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) geoData.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        geoData.setLayoutParams(params);
        refresh(post);
    }

    @Override
    public void refresh(final Post post) {
        text.setText(post.getText());
        data.setText(Utils.getDateFormatted(post.getDate()));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FullPostInfoActivity.class);
                intent.putExtra(FullPostInfoActivity.POST_KEY, post);
                getActivity().startActivity(intent);
            }
        });
        loadPictures(post);
        handleButtons(post);
    }

    private void loadPictures(final Post post) {
        int resource = Utils.getResourceByNetwork(post.getNetwork());
        socialIcon.setImageResource(resource);

        if (post.getAttachments().size() != 0) {
            final Image image = (Image) post.getAttachments().get(0);
            postImageView.loadImage(image);
        } else {
            postImageView.loadImage(null);
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

    public void setLikesOnClick(View.OnClickListener listener) {
        likesButton.setOnClickListener(listener);
    }

    public void setRepostsOnClick(View.OnClickListener listener) {
        repostsButton.setOnClickListener(listener);
    }

    public void setDeleteOnClick(View.OnClickListener listener) {
        deleteButton.setOnClickListener(listener);
    }

    public void showDeleteButton() {
        deleteButton.setVisibility(View.VISIBLE);
    }

    public void hideDeleteButton() {
        deleteButton.setVisibility(View.GONE);
    }
}
