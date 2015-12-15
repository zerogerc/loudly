package ly.loud.loudly;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import base.Tasks;
import base.attachments.Image;
import base.says.LoudlyPost;
import base.says.Post;
import ly.loud.loudly.PeopleList.PeopleListFragment;
import util.Utils;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;
    private int lastPosition = -1;
    MainActivity activity;

    RecyclerViewAdapter(List<Post> posts, MainActivity act) {
        this.posts = posts;
        activity = act;
    }

    public void notifyDeletedAtPosition(int pos) {
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, posts.size());
    }

    private void setViewSizesForImageSizes(final ViewHolder holder, final int width, final int height) {
        int imageWidth = Utils.getDefaultScreenWidth() - 2 * holder.getMargin();
        float scale = ((float) imageWidth) / ((float) width);
        int imageHeight = (int) (height * scale);
        holder.postImageView.getLayoutParams().width = imageWidth;
        holder.postImageView.getLayoutParams().height = imageHeight;
        holder.postImageView.requestLayout();
    }

    private void resizeImageView(final ViewHolder holder, final Post post) {
        if (post.getAttachments().size() != 0) {
            if (post.getAttachments().get(0) instanceof Image) {
                Image image = ((Image) post.getAttachments().get(0));
                int width = image.getWidth();
                int height = image.getHeight();

                if (width != 0 && height != 0) {
                    setViewSizesForImageSizes(holder, image.getWidth(), image.getHeight());
                } else {
                    holder.postImageView.setImageBitmap(null);
                    FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) holder.postImageView.getLayoutParams());
                    lp.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    lp.width = FrameLayout.LayoutParams.WRAP_CONTENT;
                    holder.postImageView.setLayoutParams(lp);
                }
            }
        } else {
            holder.postImageView.setImageBitmap(null);
            holder.postImageView.getLayoutParams().width = 0;
            holder.postImageView.getLayoutParams().height = 0;
            holder.postImageView.requestLayout();
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

    private void refreshFields(final ViewHolder holder, final Post post) {
        holder.text.setText(post.getText());

        holder.data.setText(Utils.getDateFormatted(post.getDate()));

        setOverShadowFooterComponent(holder.commentsButton, holder.commentsAmount, true);
        setOverShadowFooterComponent(holder.likesButton, holder.likesAmount, true);
        setOverShadowFooterComponent(holder.repostsButton, holder.repostsAmount, true);

        int resource = Utils.getResourceByNetwork(post instanceof LoudlyPost ? -1 : post.getNetwork());

        Glide.with(Loudly.getContext()).load("image")
                .error(resource)
                .placeholder(resource)
                .into(holder.socialIcon);

        if (post.getInfo() != null) {
            holder.commentsAmount.setText(Integer.toString(post.getInfo().comment));
            holder.likesAmount.setText(Integer.toString(post.getInfo().like));
            holder.repostsAmount.setText(Integer.toString(post.getInfo().repost));
        } else {
            holder.commentsAmount.setText("0");
            holder.likesAmount.setText("0");
            holder.repostsAmount.setText("0");
        }

        if (post.getAttachments().size() != 0) {
            holder.postImageView.setImageBitmap(null);

            final Image image = (Image) post.getAttachments().get(0);
            resizeImageView(holder, post);

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
                    .into(holder.postImageView);
        } else {
            holder.postImageView.setImageBitmap(null);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
            holder.postImageView.setLayoutParams(layoutParams);
        }

        holder.showMoreOptions.setOnClickListener(makeDeleteClickListener(post, activity));

        if (post.getInfo().comment != 0) {
            holder.commentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showComments(activity, post);
                }
            });
        } else {
            setOverShadowFooterComponent(holder.commentsButton, holder.commentsAmount, false);
        }

        if (post.getInfo().like != 0) {
            holder.likesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, post, Tasks.LIKES);
                }
            });
        } else {
            setOverShadowFooterComponent(holder.likesButton, holder.likesAmount, false);
        }

        if (post.getInfo().repost != 0) {
            holder.repostsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, post, Tasks.SHARES);
                }
            });
        } else {
            setOverShadowFooterComponent(holder.repostsButton, holder.repostsAmount, false);
        }

    }

    private View.OnClickListener makeDeleteClickListener(final Post post, final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Delete post?")
                        .setMessage("Do you want to delete this post from all networks?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Loudly.getContext().stopGetInfoService();
                                activity.floatingActionButton.hide();
                                MainActivity.receivers[MainActivity.POST_DELETE_RECEIVER] =
                                        new MainActivity.PostDeleteReceiver(context);
                                new Tasks.PostDeleter(post, MainActivity.posts, Loudly.getContext().getWraps()).
                                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new ViewHolder(v, new LoudlyPost("Hello world!!!"));
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = posts.get(position);
        refreshFields(holder, post);
        setAnimation(holder.root, position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View root;
        private ImageView socialIcon;
        private TextView text;
        private TextView data;
        private TextView geoData;
        private TextView commentsAmount;
        private ImageView commentsButton;
        private TextView likesAmount;
        private ImageView likesButton;
        private TextView repostsAmount;
        private ImageView repostsButton;
        private ImageView postImageView;
        private ImageView showMoreOptions;

        public int getMargin() {
            return Utils.dpToPx(8);
        }

        public ViewHolder(View itemView, final Post post) {
            super(itemView);

            root = itemView;
            socialIcon = (ImageView) root.findViewById(R.id.post_view_social_network_icon);
            text = (TextView) root.findViewById(R.id.post_view_post_text);
            data = (TextView) root.findViewById(R.id.post_view_data_text);
            geoData = (TextView) root.findViewById(R.id.post_view_geo_data_text);
            commentsAmount = (TextView) root.findViewById(R.id.post_view_comments_amount);
            commentsButton = (ImageView) root.findViewById(R.id.post_view_comments_button);
            likesAmount = (TextView) root.findViewById(R.id.post_view_likes_amount);
            likesButton = (ImageView) root.findViewById(R.id.post_view_likes_button);
            repostsAmount = (TextView) root.findViewById(R.id.post_view_reposts_amount);
            repostsButton = (ImageView) root.findViewById(R.id.post_view_reposts_button);
            postImageView = (ImageView) root.findViewById(R.id.post_view_post_image);
            showMoreOptions = (ImageView) root.findViewById(R.id.post_view_more_options_button);

            geoData.setHeight(0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) geoData.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            geoData.setLayoutParams(params);
            refreshFields(this, post);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(viewToAnimate, "translationY", Utils.getDefaultScreenHeight(), 0);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
//            Animation animation = AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left);
//            viewToAnimate.startAnimation(animation);
            animator.start();
            lastPosition = position;
            Log.e("POS", Integer.toString(lastPosition));
        }
    }
}
