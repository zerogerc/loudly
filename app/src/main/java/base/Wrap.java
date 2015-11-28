package base;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import Facebook.FacebookWrap;
import VK.VKWrap;
import base.attachments.Image;
import util.BackgroundAction;
import util.Query;
import util.TimeInterval;

/**
 * Base interface for all interactions with particular social network.
 * Uses KeyKeepers, stored in Loudly Application
 */

public abstract class Wrap {
    /**
     * @return ID of the network (from Networks class)
     */
    public abstract int networkID();

    protected abstract Query makeAPICall(String url);
    protected abstract Query makeSignedAPICall(String url);

    public abstract void uploadPost(Post post) throws IOException;

    public abstract void uploadImage(Image image, BackgroundAction progress) throws IOException;

    public abstract void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback) throws IOException;

    public abstract void getPostsInfo(Post... posts) throws IOException;


    public static Wrap makeWrap(int network) {
        switch (network) {
            case Networks.FB:
                return new FacebookWrap();
            case Networks.VK:
                return new VKWrap();
            default:
                return null;
        }
    }
}
