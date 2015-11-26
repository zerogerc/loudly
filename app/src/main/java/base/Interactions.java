package base;

import java.io.IOException;
import java.util.LinkedList;

import Facebook.FacebookWrap;
import base.attachments.Attachment;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import util.BackgroundAction;
import util.IDInterval;
import util.Interval;
import util.Network;
import util.Query;
import util.TimeInterval;

/**
 * Class that contains simple actions that can be done with social network, such as
 * post the post, get likes or shares
 */
public class Interactions {
    /**
     * Post posts to one network
     *
     * @param wrap    Wrap for the network
     * @param post    Post
     * @param publish Action for publishing result
     */
    public static void post(Wrap wrap, Post post, final BackgroundAction publish) throws IOException {
        final Post.Counter counter = post.getCounter();
        Integer k = 0;
        for (Attachment attachment : post.getAttachments()) {
            if (attachment instanceof Image) {
                k++;
                final double multiplier = k / (counter.imageCount + 1);
                wrap.uploadImage((Image) attachment, new BackgroundAction() {
                    @Override
                    public void execute(Object... params) {
                        // ToDo: do it later
                    }
                });
            }
        }
        String response = Network.makePostRequest(wrap.makePostQuery(post), new BackgroundAction() {
            @Override
            public void execute(Object... params) {
                publish.execute(params[0]);
            }
        });
        wrap.parsePostResponse(post, response);
    }

    public static void getInfo(Wrap wrap, Post post) throws IOException {
        Query queries = wrap.makeGetQueries(post);
        String response = Network.makeGetRequest(queries);
        wrap.parseGetResponse(post, response);
    }

    public static void deletePost(Wrap wrap, Post post) throws IOException {
        Query query = wrap.makeDeleteQuery(post);
        String response = Network.makeGetRequest(query);
        wrap.parseDeleteResponse(post, response);
    }

    public static void deletePost(FacebookWrap wrap, Post post) throws IOException {
        Query query = wrap.makeDeleteQuery(post);
        String response = Network.makeDeleteRequest(query);
        wrap.parseDeleteResponse(post, response);
    }

    public static void loadPosts(Wrap wrap, TimeInterval time,
                                             Tasks.LoadCallback callback) throws IOException {
        TimeInterval loadedTimeInterval = time.copy();
        boolean hasMore;
        do {
            Query query = wrap.makeLoadPostsQuery(loadedTimeInterval);
            String response = Network.makeGetRequest(query);
            hasMore = wrap.parsePostsLoadedResponse(loadedTimeInterval, response, callback);
        } while (hasMore);
    }
}
