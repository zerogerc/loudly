package ly.loud.loudly;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        private TextView likes;
        private TextView reposts;

        public ViewHolder(View itemView, Post post) {
            super(itemView);

            text = (TextView)itemView.findViewById(R.id.post_text);
            data = (TextView)itemView.findViewById(R.id.data);
            geoData = (TextView)itemView.findViewById(R.id.geo_data);
            likes = (TextView)itemView.findViewById(R.id.likes);
            reposts = (TextView)itemView.findViewById(R.id.reposts);

            text.setText(post.getText());
        }
    }
}
