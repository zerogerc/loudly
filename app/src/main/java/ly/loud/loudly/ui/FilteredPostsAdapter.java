package ly.loud.loudly.ui;

import com.android.internal.util.Predicate;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.util.FilteredListView;

import java.util.List;

/**
 * @author Danil Kolikov
 */
public class FilteredPostsAdapter extends PostsAdapter {
    private int network;
    private Predicate<Post> predicate;

    public FilteredPostsAdapter(List<Post> posts, final int networkFilter, MainActivity activity) {
        super(new FilteredListView<>(posts, new Predicate<Post>(){
            @Override
            public boolean apply(Post post) {
                return post.getNetworkInstance(networkFilter) != null;
            }
        }), activity);
        network = networkFilter;
        predicate = new Predicate<Post>() {
            @Override
            public boolean apply(Post post) {
                return post.getNetworkInstance(networkFilter) != null;
            }
        };
    }

    @Override
    protected Post getPost(int position) {
        return (Post)super.getPost(position).getNetworkInstance(network);
    }

    @Override
    public void update(List<? extends Post> updated) {
        super.update(new FilteredListView<>(updated, predicate));
    }

    @Override
    public void insert(List<? extends Post> inserted) {
        super.insert(new FilteredListView<>(inserted, predicate));
    }

    @Override
    public void delete(List<? extends Post> deleted) {
        super.delete(new FilteredListView<>(deleted, predicate));
    }
}
