package ly.loud.loudly;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import base.Post;
import base.attachments.Image;
import util.Utils;
import util.picasso.CircleTransform;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;

    RecyclerViewAdapter(List<Post> posts) {
        this.posts = posts;
    }

    private void refreshFields(final ViewHolder holder, final Post post) {
        holder.text.setText(post.getText());

        holder.data.setText(getDateFormatted(post.getDate()));

        int resource = Utils.getIconDrawbleByNetwork(post.getMainNetwork());
        Picasso.with(Loudly.getContext()).load("image")
                .error(resource)
                .placeholder(resource)
                .into(holder.socialIcon);

        if (post.getTotalInfo() != null) {
            holder.commentsAmount.setText(Integer.toString(post.getTotalInfo().comment));
            holder.likesAmount.setText(Integer.toString(post.getTotalInfo().like));
            holder.repostsAmount.setText(Integer.toString(post.getTotalInfo().repost));
        } else {
            holder.commentsAmount.setText("0");
            holder.likesAmount.setText("0");
            holder.repostsAmount.setText("0");
        }

        if (post.getAttachments().size() != 0) {
            holder.postImageView.setImageBitmap(null);
            post.setLoadedImage(false);
            holder.progressBar.setVisibility(View.VISIBLE);

            Image image = (Image)post.getAttachments().get(0);
            Picasso.with(Loudly.getContext()).load(image.getUri()).
                    resize(Utils.getDefaultScreenWidth(), Utils.getDefaultScreenHeight())
                    .transform(new CircleTransform())
                            .centerInside()
                    .into(holder.postImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onError() {
                            Toast.makeText(Loudly.getContext(), "Error occured during image load", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            post.setLoadedImage(true);
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            holder.postImageView.setImageBitmap(null);
        }

        if (post.isLoadedImage()) {
            holder.progressBar.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private String getDateFormatted(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date * 1000);
        return cal.get(Calendar.DAY_OF_MONTH) + "." + + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR)
                + " around " + cal.get(Calendar.HOUR_OF_DAY) + " hours";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_view, parent, false);
        return new ViewHolder(v, new Post("Hello world!!!"));
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = posts.get(position);
        refreshFields(holder, post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
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
        private ProgressBar progressBar;

        public ViewHolder(View itemView, Post post) {
            super(itemView);

            socialIcon = (ImageView)itemView.findViewById(R.id.post_view_social_network_icon);
            text = (TextView)itemView.findViewById(R.id.post_view_post_text);
            data = (TextView)itemView.findViewById(R.id.post_view_data_text);
            geoData = (TextView)itemView.findViewById(R.id.post_view_geo_data_text);
            commentsAmount = (TextView)itemView.findViewById(R.id.post_view_comments_amount);
            commentsButton = (ImageView)itemView.findViewById(R.id.post_view_comments_button);
            likesAmount = (TextView)itemView.findViewById(R.id.post_view_likes_amount);
            likesButton = (ImageView)itemView.findViewById(R.id.post_view_likes_button);
            repostsAmount = (TextView)itemView.findViewById(R.id.post_view_reposts_amount);
            repostsButton = (ImageView)itemView.findViewById(R.id.post_view_reposts_button);
            postImageView = (ImageView)itemView.findViewById(R.id.post_view_post_image);
            progressBar = (ProgressBar)itemView.findViewById(R.id.post_view_progress);

            geoData.setHeight(0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)geoData.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            geoData.setLayoutParams(params);
            refreshFields(this, post);
        }
    }
}
