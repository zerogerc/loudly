package ly.loud.loudly.ui;

import com.android.internal.util.Predicate;

import java.util.List;

import ly.loud.loudly.new_base.LoudlyPost;
import ly.loud.loudly.new_base.SinglePost;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.util.FilteredListView;

/**
 * @author Danil Kolikov
 */
public class FilteredPostsAdapter extends PostsAdapter {
    private int network;
    private Predicate<PlainPost> predicate;

    public FilteredPostsAdapter(List<PlainPost> posts, final int networkFilter, MainActivity activity) {
        super(new FilteredListView<>(posts, post -> {
            if (post instanceof SinglePost) {
                return ((SinglePost) post).getNetwork() == networkFilter;
            }
            if (post instanceof LoudlyPost) {
                return ((LoudlyPost) post).getSingleNetworkInstance(networkFilter) != null;
            }
            return false;
        }), activity);
        network = networkFilter;
        predicate = post -> {
            if (post instanceof SinglePost) {
                return ((SinglePost) post).getNetwork() == networkFilter;
            }
            if (post instanceof LoudlyPost) {
                return ((LoudlyPost) post).getSingleNetworkInstance(networkFilter) != null;
            }
            return false;
        };
    }

    @Override
    protected PlainPost getPost(int position) {
        return super.getPost(position);
    }

    @Override
    public void update(List<? extends PlainPost> updated) {
        super.update(new FilteredListView<>(updated, predicate));
    }

    @Override
    public void insert(List<? extends PlainPost> inserted) {
        super.insert(new FilteredListView<>(inserted, predicate));
    }

    @Override
    public void delete(List<? extends PlainPost> deleted) {
        super.delete(deleted);
    }
}
