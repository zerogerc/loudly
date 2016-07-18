package ly.loud.loudly.ui;

import com.android.internal.util.Predicate;

import java.util.List;

import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.util.FilteredListView;

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
                SingleNetwork network = post.getNetworkInstance(networkFilter);
                return network != null;
            }
        }), activity);
        network = networkFilter;
        predicate = new Predicate<Post>() {
            @Override
            public boolean apply(Post post) {
                SingleNetwork network = post.getNetworkInstance(networkFilter);
                return network != null;
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
        super.delete(deleted);
    }
}
