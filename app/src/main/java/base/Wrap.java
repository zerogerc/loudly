package base;

import java.io.IOException;
import java.util.List;

import base.attachments.Image;
import base.attachments.LoudlyImage;
import base.says.Comment;
import base.says.LoudlyPost;
import base.says.Post;
import util.BackgroundAction;
import util.InvalidTokenException;
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
     * Reset fields such as offsets
     */
    public abstract void resetState();


    /**
     * @return ID of the network (from Networks class)
     */
    public abstract int networkID();


    protected abstract Query makeAPICall(String url);

    protected abstract Query makeSignedAPICall(String url) throws InvalidTokenException;

    public abstract void uploadPost(LoudlyPost post) throws IOException;

    public abstract void uploadImage(LoudlyImage image, BackgroundAction progress) throws IOException;

    public abstract void deletePost(Post post) throws IOException;

    public abstract void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback) throws IOException;

    public abstract void getPostsInfo(List<Post> posts, Tasks.GetInfoCallback callback) throws IOException;

    public abstract List<Person> getPersons(int what, SingleNetwork element) throws IOException;

    public abstract List<Comment> getComments(SingleNetwork element) throws IOException;

    public abstract void getImageInfo(List<Image> images) throws IOException;

    public abstract String checkPost(LoudlyPost post);


    /**
     * Proper flag from this class
     */
    public abstract int shouldUploadImage();

    // Firstly upload photos to networks, that allows only upload photos
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

}
