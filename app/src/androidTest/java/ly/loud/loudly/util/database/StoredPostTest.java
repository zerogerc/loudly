package ly.loud.loudly.util.database;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.new_base.LoudlyPost;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.new_base.SinglePost;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.util.Equality;
import ly.loud.loudly.test.Generators;
import ly.loud.loudly.util.TimeInterval;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Danil Kolikov
 */
public class StoredPostTest extends DatabaseTest<LoudlyPost> {
    @CheckResult
    @NonNull
    @Override
    protected LoudlyPost generate() {
        return Generators.randomLoudlyPost(100, 20, 100, 20, random);
    }

    @CheckResult
    @NonNull
    @Override
    protected LoudlyPost get(@NonNull LoudlyPost object) throws DatabaseException {
        SingleNetworkElement element = object.getSingleNetworkInstance(Networks.LOUDLY);
        Assert.assertNotNull(element);
        String id = Link.getLink(element.getLink());
        Assert.assertNotNull(id);

        return DatabaseUtils.loadPost(Long.parseLong(id));
    }

    @Override
    protected void delete(@NonNull LoudlyPost object) throws DatabaseException {
        DatabaseUtils.deletePost(object);
    }

    @CheckResult
    @NonNull
    @Override
    protected LoudlyPost insert(@NonNull LoudlyPost object) throws DatabaseException {
        return DatabaseUtils.savePost(object);
    }

    @Override
    protected boolean equals(@Nullable LoudlyPost a, @Nullable LoudlyPost b) {
        return Equality.equal(a, b);
    }

    @Test
    public void testLoadByTime() throws DatabaseException {
        ArrayList<LoudlyPost> posts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            LoudlyPost random = Generators.randomLoudlyPost(20, 20, 20, 20, this.random);
            LoudlyPost post = new LoudlyPost(random.getText(), i, random.getAttachments(), random.getLocation());
            for (int j = 0; j < Networks.NETWORK_COUNT; j++) {
                SinglePost instance = random.getSingleNetworkInstance(j);
                if (instance!= null) {
                    post = post.setSingleNetworkInstance(j, new SinglePost(post.getText(), post.getDate(),
                            instance.getAttachments(), post.getLocation(),
                            j, instance.getLink()));
                }
            }
            posts.add(insert(post));
        }
        int begin = random.nextInt(100);
        int end = begin + random.nextInt(100 - begin);
        List<LoudlyPost> sublist = new ArrayList<>();
        for (LoudlyPost post : posts) {
            if (begin < post.getDate() && post.getDate() < end) {
                sublist.add(post);
            }
        }
        List<LoudlyPost> stored = DatabaseUtils.loadPosts(new TimeInterval(begin, end));
        Assert.assertSame(sublist.size(), stored.size());
        for (int i = 0; i < sublist.size(); i++) {
            // Sublist has reversed order
            Assert.assertTrue(Equality.equal(sublist.get(sublist.size() - i - 1), stored.get(i)));
        }
    }
}