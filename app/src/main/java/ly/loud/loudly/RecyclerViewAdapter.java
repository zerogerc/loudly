package ly.loud.loudly;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import base.Post;
import base.Tasks;
import base.attachments.Image;
import util.AttachableTask;
import util.Utils;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;
    MainActivity activity;

    RecyclerViewAdapter(List<Post> posts, MainActivity act) {
        this.posts = posts;
        activity = act;
    }

    private void refreshFields(final ViewHolder holder, final Post post) {
        holder.text.setText(post.getText());

        holder.data.setText(getDateFormatted(post.getDate()));

        int resource = Utils.getResourceByNetwork(post.getMainNetwork());

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

            final Image image = (Image)post.getAttachments().get(0);

            if (image.getHeight() == 0 && image.getWidth() == 0) {
                AttachableTask<Void, Void, Void> task = new AttachableTask<Void, Void, Void>(Loudly.getContext()) {
                    @Override
                    public void executeInUI(Context context, Void aVoid) {
                        notifyDataSetChanged();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        image.setSize(Utils.resolveImageSize(image));
                        return null;
                    }
                };

                task.execute();
            }

            double width = image.getWidth();
            double height = image.getHeight();

            if (width != 0) {
                double scale = (double)Utils.getDefaultScreenWidth() / width;

                width = Utils.getDefaultScreenWidth();
                height = height * scale;
            }

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int)width, (int)height);
            holder.postImageView.setLayoutParams(layoutParams);

            Picasso.with(Loudly.getContext()).load(image.getUri()).
                    resize(Utils.getDefaultScreenWidth(), Utils.getDefaultScreenHeight())
                    .centerInside()
                    .priority(Picasso.Priority.HIGH)
                    .into(holder.postImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onError() {
                            Toast.makeText(Loudly.getContext(), "Error occured during image load", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            post.setLoadedImage(true);
                        }
                    });
        } else {
            holder.postImageView.setImageBitmap(null);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
            holder.postImageView.setLayoutParams(layoutParams);
        }


        holder.likesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.peopleListFragment.showPersons(post, Tasks.LIKES);
            }
        });

        holder.repostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.peopleListFragment.showPersons(post, Tasks.SHARES);
            }
        });

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

        public ViewHolder(View itemView, final Post post) {
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

            geoData.setHeight(0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)geoData.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            geoData.setLayoutParams(params);
            refreshFields(this, post);
        }
    }
}
