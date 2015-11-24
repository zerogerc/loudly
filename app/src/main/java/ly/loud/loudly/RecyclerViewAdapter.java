package ly.loud.loudly;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import base.Networks;
import base.Post;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;

    RecyclerViewAdapter(List<Post> posts) {
        this.posts = posts;
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
        holder.text.setText(post.getText());

        holder.data.setText(getDateFormatted(post.getDate()));

        if (post.getMainNetwork() == Networks.FB) {
            holder.socialIcon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_instagram_round));
        } else {
            holder.socialIcon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_mail_ru_round));
        }

        int likes_amount = 0;
        int resposts_amount = 0;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            Post.Info info = post.getInfo(i);
            if (info != null) {
                likes_amount += info.like;
                resposts_amount += info.repost;
            }
        }

        holder.likesAmount.setText(Integer.toString(likes_amount));
        holder.repostsAmount.setText(Integer.toString(resposts_amount));
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
        private TextView likesAmount;
        private ImageView likesButton;
        private TextView repostsAmount;
        private ImageView repostsButton;

        public ViewHolder(View itemView, Post post) {
            super(itemView);

            socialIcon = (ImageView)itemView.findViewById(R.id.post_view_social_network_icon);
            text = (TextView)itemView.findViewById(R.id.post_view_post_text);
            data = (TextView)itemView.findViewById(R.id.post_view_data_text);
            geoData = (TextView)itemView.findViewById(R.id.post_view_geo_data_text);
            likesAmount = (TextView)itemView.findViewById(R.id.post_view_likes_amount);
            likesButton = (ImageView)itemView.findViewById(R.id.post_view_likes_button);
            repostsAmount = (TextView)itemView.findViewById(R.id.post_view_reposts_amount);
            repostsButton = (ImageView)itemView.findViewById(R.id.post_view_reposts_button);

            if (Math.random() > 0.5) {
                socialIcon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.mipmap.ic_instagram_round));
            } else {
                socialIcon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.mipmap.ic_mail_ru_round));
            }

            //TODO here MUST be if
            geoData.setHeight(0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)geoData.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            geoData.setLayoutParams(params);

            data.setText(getDateFormatted(post.getDate()));
            text.setText(post.getText());

            int likes_amount = 0;
            int resposts_amount = 0;
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                Post.Info info = post.getInfo(i);
                if (info != null) {
                    likes_amount += info.like;
                    resposts_amount += info.repost;
                }
            }

            likesAmount.setText(Integer.toString(likes_amount));
            repostsAmount.setText(Integer.toString(resposts_amount));

        }
    }
}
