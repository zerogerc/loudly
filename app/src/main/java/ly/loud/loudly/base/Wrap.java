package ly.loud.loudly.base;

import android.support.annotation.NonNull;
import android.util.Pair;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.ui.Loudly;
import ly.loud.loudly.util.BackgroundAction;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.TimeInterval;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Base interface for all interactions with particular social network.
 * Uses KeyKeepers, stored in Loudly Application
 */

public abstract class Wrap implements Comparable<Wrap> {
    public static final int IMAGE_ONLY_UPLOAD = 0;
    public static final int IMAGE_UPLOAD_OR_LINK = 1;
    public static final int IMAGE_ONLY_LINK = 2;

    public static final int UPLOAD_LAST = 1337;
    // Interface

    /**
     * Reset fields such as offsets
     */
    public abstract void resetState();

    /**
     * @return ID of the network (from Networks class)
     */
    public abstract int networkID();


    /**
     * Proper flag from this class
     */
    public abstract int shouldUploadImage();

    public abstract String checkPost(LoudlyPost post);

    // Realisation

    protected abstract Query makeAPICall(String url);

    protected abstract Query makeSignedAPICall(String url, KeyKeeper keyKeeper);

    protected abstract void upload(Post post, KeyKeeper keyKeeper) throws IOException;

    protected abstract void upload(Image image, BackgroundAction progress, KeyKeeper keyKeeper) throws IOException;

    protected abstract void delete(Post post, KeyKeeper keyKeeper) throws IOException;

    protected abstract List<Post> loadPosts(TimeInterval timeInterval, KeyKeeper keyKeeper) throws IOException;

    /**
     * Handle ERROR answer from network
     *
     * @param stream ErrorStream from network
     * @return User-friendly description of error
     * @throws IOException If some error occurs
     */
    public abstract String handleError(InputStream stream) throws IOException;


    /**
     * Get changed info for posts
     *
     * @param posts     Lists of Posts to which should get updated info
     * @param keyKeeper Actual keykeeper with user ID
     * @return List of pair of posts, which info have changed, and new info. If info isn't got, return pair <Post, null>
     * @throws IOException in case of network exception
     */
    protected abstract List<Pair<Post, Info>> getPostsInfo(List<Post> posts, KeyKeeper keyKeeper) throws IOException;

    protected abstract List<Person> getPersons(int what, SingleNetwork element, KeyKeeper keyKeeper) throws IOException;

    protected abstract List<Comment> getComments(SingleNetwork element, KeyKeeper keyKeeper) throws IOException;

    protected abstract void getImageInfo(List<Image> images, KeyKeeper keyKeeper) throws IOException;

    public void upload(final Post post) throws IOException {
        doWithKeys(new KeyKeeper.Action<Void>() {
            @Override
            public Void execute(KeyKeeper keyKeeper) throws IOException {
                upload(post, keyKeeper);
                return null;
            }
        });
    }

    public void upload(final Image image, final BackgroundAction progress) throws IOException {
        doWithKeys(new KeyKeeper.Action<Void>() {
            @Override
            public Void execute(KeyKeeper keyKeeper) throws IOException {
                upload(image, progress, keyKeeper);
                return null;
            }
        });
    }

    public void delete(final Post post) throws IOException {
        doWithKeys(new KeyKeeper.Action<Void>() {
            @Override
            public Void execute(KeyKeeper keyKeeper) throws IOException {
                delete(post, keyKeeper);
                return null;
            }
        });
    }

    public List<Post> loadPosts(final TimeInterval timeInterval) throws IOException {
        return doWithKeys(new KeyKeeper.Action<List<Post>>() {
            @Override
            public List<Post> execute(KeyKeeper keyKeeper) throws IOException {
                return loadPosts(timeInterval, keyKeeper);
            }
        });
    }

    public List<Pair<Post, Info>> getPostsInfo(final List<Post> posts) throws IOException {
        return doWithKeys(new KeyKeeper.Action<List<Pair<Post, Info>>>() {
            @Override
            public List<Pair<Post, Info>> execute(KeyKeeper keyKeeper) throws IOException {
                return getPostsInfo(posts, keyKeeper);
            }
        });
    }

    public List<Person> getPersons(final int what, final SingleNetwork element) throws IOException {
        return doWithKeys(new KeyKeeper.Action<List<Person>>() {
            @Override
            public List<Person> execute(KeyKeeper keyKeeper) throws IOException {
                return getPersons(what, element, keyKeeper);
            }
        });
    }

    public List<Comment> getComments(final SingleNetwork element) throws IOException {
        return doWithKeys(new KeyKeeper.Action<List<Comment>>() {
            @Override
            public List<Comment> execute(KeyKeeper keyKeeper) throws IOException {
                return getComments(element, keyKeeper);
            }
        });
    }

    public void updateImagesInfo(final List<Image> images) throws IOException {
        doWithKeys(new KeyKeeper.Action<Void>() {
            @Override
            public Void execute(KeyKeeper keyKeeper) throws IOException {
                getImageInfo(images, keyKeeper);
                return null;
            }
        });
    }


    private <T> T doWithKeys(KeyKeeper.Action<T> action) throws IOException {
        KeyKeeper keys = Loudly.getContext().getKeyKeeper(networkID());
        if (keys == null) {
            return null;
        }
        return keys.doWithKeys(action);
    }


    // Firstly upload photos to networks, that allows only upload photos
    @Override
    public int compareTo(@NonNull Wrap another) {
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
