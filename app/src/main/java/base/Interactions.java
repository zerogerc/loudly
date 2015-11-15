package base;

import java.io.IOException;

import base.attachments.Attachment;
import base.attachments.Image;
import util.BackgroundAction;
import util.Counter;
import util.Network;

/**
 * Class that contains simple actions that can be done with social network, such as
 * post the post, get likes or shares
 */
public class Interactions {
    /**
     * Post posts to one network
     *
     * @param wrap    Wrappable for the network
     * @param post    Post
     * @param publish Action for publishing result
     */
    public static void post(Wrappable wrap, Post post, final BackgroundAction publish) throws IOException {
        final Counter counter = post.getCounter();
        Integer k = 0;
        for (Attachment attachment : post.getAttachments()) {
            if (attachment instanceof Image) {
                k++;
                final double multiplier = k / (counter.getImageCount() + 1);
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
        wrap.parseResponse(post, response);
    }


}
