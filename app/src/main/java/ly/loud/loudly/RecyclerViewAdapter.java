package ly.loud.loudly;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import base.Post;
import base.attachments.Image;
import util.Utils;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;

    RecyclerViewAdapter(List<Post> posts) {
        this.posts = posts;
    }

    private void refreshFields(ViewHolder holder, Post post) {
        holder.text.setText(post.getText());

        holder.data.setText(getDateFormatted(post.getDate()));

        holder.socialIcon.setImageBitmap(Utils.getIconByNetwork(post.getMainNetwork()));

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
            Image image = (Image)post.getAttachments().get(0);
            holder.postImageView.setImageBitmap(image.getBitmap());
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
