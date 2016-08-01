package ly.loud.loudly.networks.Loudly;

import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import ly.loud.loudly.base.*;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.new_base.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.new_base.KeyKeeper;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.new_base.Person;
import ly.loud.loudly.util.BackgroundAction;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.database.DatabaseUtils;

/**
 * Wrap over database
 */
public class LoudlyWrap extends Wrap {
    private static final NetworkDescription DESCRIPTION = new NetworkDescription() {
        @Override
        public boolean canPost() {
            return true;
        }

        @Override
        public boolean canDelete() {
            return true;
        }
    };

    @Override
    public NetworkDescription getDescription() {
        return DESCRIPTION;
    }

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
//        DatabaseUtils.savePost(((LoudlyPost) post));
    }

    @Override
    protected void upload(Image image, BackgroundAction progress, KeyKeeper keyKeeper) throws IOException {
        // Now it's not like other networks, it saves attachments after uploading
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
//        DatabaseUtils.deletePost(loudlyPost);
        loudlyPost.getLink(Networks.LOUDLY).setValid(false);
    }

    @Override
    protected List<Post> loadPosts(TimeInterval timeInterval, KeyKeeper keyKeeper) throws IOException {
//        return DatabaseUtils.loadPosts(timeInterval);
        return Collections.emptyList();
    }

    @Override
    protected List<Pair<Post, Info>> getPostsInfo(List<Post> posts, KeyKeeper keyKeeper) throws IOException {
        // Can't get nothing new
        return null;
    }

    @Override
    protected List<Person> getPersons(int what, SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        return Collections.emptyList();
    }

    @Override
    protected List<Comment> getComments(SingleNetwork element, KeyKeeper keyKeeper) throws IOException {
        return Collections.emptyList();
    }

    @Override
    protected void getImageInfo(List<Image> images, KeyKeeper keyKeeper) throws IOException {
        // Can't load image info
    }

    @Override
    public String handleError(InputStream stream) {
        // Nothing can be handled
        return null;
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
