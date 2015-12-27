package base;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import base.attachments.Image;
import base.attachments.LoudlyImage;
import base.says.Comment;
import base.says.LoudlyPost;
import base.says.Post;
import ly.loud.loudly.Loudly;
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

    protected abstract void loadPosts(TimeInterval timeInterval, Tasks.LoadCallback callback, KeyKeeper keyKeeper) throws IOException;

    protected abstract void getPostsInfo(List<Post> posts, Tasks.GetInfoCallback callback, KeyKeeper keyKeeper) throws IOException;

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

    public void loadPosts(final TimeInterval timeInterval, final Tasks.LoadCallback callback) throws IOException {
        doWithKeys(new KeyKeeper.Action<Void>() {
            @Override
            public Void execute(KeyKeeper keyKeeper) throws IOException {
                loadPosts(timeInterval, callback, keyKeeper);
                return null;
            }
        });
    }

    public void getPostsInfo(final List<Post> posts, final Tasks.GetInfoCallback callback) throws IOException {
        doWithKeys(new KeyKeeper.Action<Void>() {
            @Override
            public Void execute(KeyKeeper keyKeeper) throws IOException {
                getPostsInfo(posts, callback, keyKeeper);
                return null;
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
            throw new InvalidTokenException();
        }
        if (!keys.isValid()) {
            throw new InvalidTokenException();
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
