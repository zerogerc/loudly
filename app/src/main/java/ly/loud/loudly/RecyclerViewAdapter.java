package ly.loud.loudly;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import base.Post;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Post> posts;

    RecyclerViewAdapter(List<Post> posts) {
        this.posts = posts;
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
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private TextView data;
        private TextView geoData;
        private TextView likes_amount;
        private ImageButton likes_button;
        private TextView reposts_amount;
        private ImageButton reposts_button;

        public ViewHolder(View itemView, Post post) {
            super(itemView);

            text = (TextView)itemView.findViewById(R.id.post_view_post_text);
            data = (TextView)itemView.findViewById(R.id.post_view_data_text);
            geoData = (TextView)itemView.findViewById(R.id.post_view_geo_data_text);
            likes_amount = (TextView)itemView.findViewById(R.id.post_view_likes_amount);
            likes_button = (ImageButton)itemView.findViewById(R.id.post_view_likes_button);
            reposts_amount = (TextView)itemView.findViewById(R.id.post_view_reposts_amount);
            reposts_button = (ImageButton)itemView.findViewById(R.id.post_view_reposts_button);

            text.setText(post.getText());
        }
    }
}
