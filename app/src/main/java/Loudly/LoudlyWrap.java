package Loudly;

import android.util.Pair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import base.KeyKeeper;
import base.Networks;
import base.Person;
import base.SingleNetwork;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import base.attachments.LoudlyImage;
import base.says.Comment;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import util.BackgroundAction;
import util.Query;
import util.TimeInterval;
import util.database.DatabaseActions;

/**
 * Wrap over database
 */
public class LoudlyWrap extends Wrap {
    @Override
    protected Query makeAPICall(String url) {
        return null;
    }

    @Override
    protected Query makeSignedAPICall(String url, KeyKeeper keyKeeper) {
        return null;
    }

    @Override
    protected void upload(Post post, KeyKeeper keyKeeper) throws IOException {
        DatabaseActions.savePost(((LoudlyPost) post));
    }

    @Override
    protected void upload(Image image, BackgroundAction progress, KeyKeeper keyKeeper) throws IOException {
        DatabaseActions.saveAttachment((LoudlyImage) image);
    }

    @Override
    protected void delete(Post post, KeyKeeper keyKeeper) throws IOException {
        LoudlyPost loudlyPost = ((LoudlyPost) post);

        // If post exists in at least one network (not loudly), do nothing
        for (int i = 1; i < Networks.NETWORK_COUNT; i++) {
            if (loudlyPost.existsIn(i)) {
                return;
            }
        }
        DatabaseActions.deletePost(loudlyPost);
    }

    @Override
    protected List<Post> loadPosts(TimeInterval timeInterval, KeyKeeper keyKeeper) throws IOException {
        return DatabaseActions.loadPosts(timeInterval);
    }

    @Override
    protected List<Pair<Post, Info>> getPostsInfo(List<Post> posts, KeyKeeper keyKeeper) throws IOException {
        // Can't get nothing new
        return null;
    }

    @Override
    protected List<Person> getPersons(int what, SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        return null;
    }

    @Override
    protected List<Comment> getComments(SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        return null;
    }

    @Override
    protected void getImageInfo(List<Image> images, KeyKeeper keyKeeper) throws IOException {
        // Can't load image info
    }

    @Override
    public void resetState() {
        // Nothing to reset
    }

    @Override
    public int networkID() {
        return Networks.LOUDLY;
    }

    @Override
    public int shouldUploadImage() {
        return UPLOAD_LAST;
    }

    @Override
    public String checkPost(LoudlyPost post) {
        // We can save everything
        return null;
    }
}
