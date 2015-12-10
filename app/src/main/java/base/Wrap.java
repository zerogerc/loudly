package base;

import java.io.IOException;
import java.util.List;

import Facebook.FacebookWrap;
import VK.VKWrap;
import base.attachments.Image;
import base.says.Comment;
import base.says.LoudlyPost;
import base.says.Post;
import util.BackgroundAction;
import util.Query;
import util.TimeInterval;

/**
 * Base interface for all interactions with particular social network.
 * Uses KeyKeepers, stored in Loudly Application
 */

public abstract class Wrap implements Comparable<Wrap> {
    public static final int IMAGE_ONLY_UPLOAD = 0;
    public static final int IMAGE_UPLOAD_OR_LINK = 1;
    public static final int IMAGE_ONLY_LINK = 2;

    /**
     * Proper flag from this class
     */
    public abstract int shouldUploadImage();

    // Firstly upload photos to networks, that only allows
    @Override
    public int compareTo(Wrap another) {
        int first = shouldUploadImage();
        int second = another.shouldUploadImage();
        if (first < second) {
            return -1;
        }
        if (first == second) {
            return 0;
        }
        return 1;
    }

    /**
     * @return ID of the network (from Networks class)
     */
    public abstract int networkID();

    protected abstract Query makeAPICall(String url);
    protected abstract Query makeSignedAPICall(String url);

    public abstract void uploadPost(LoudlyPost post) throws IOException;

    public abstract void uploadImage(Image image, BackgroundAction progress) throws IOException;

    public abstract void deletePost(Post post) throws IOException;

    public abstract void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback) throws IOException;

    public abstract void getPostsInfo(List<Post> posts) throws IOException;

    public abstract List<Person> getPersons(int what, Post post) throws IOException;

    public abstract List<Comment> getComments(Post post) throws IOException;

    public abstract void getImageInfo(List<Image> images) throws IOException;

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
