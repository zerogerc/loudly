package ly.loud.loudly;

import android.app.Application;

import java.util.ArrayList;
import java.util.LinkedList;

import base.KeyKeeper;
import base.Networks;
import base.Post;
import base.Tasks;
import base.Wrap;

/**
 * Core application of Loudly app
 * Stores run-time variables
 */
public class Loudly extends Application {
    public static final String SAVED_KEYS = "ly.load.loudly.keys.saved";
    public static final String LOADED_KEYS = "ly.loud.loudly.keys.loaded";

    public static final String LOADED_POSTS = "ly.loud.loudly.posts";
    public static final String AUTHORIZATION_FINISHED = "ly.loud.loudly.auth.finished";

    public static final String POST_UPLOAD_STARTED = "ly.loud.loudly.post.started";
    public static final String POST_UPLOAD_PROGRESS = "ly.loud.loudly.post.progress";
    public static final String POST_UPLOAD_FINISHED = "ly.loud.loudly.post.finished";

    public static final String POST_GET_INFO_PROGRESS = "ly.loud.loudly.post.info.progress";
    public static final String POST_GET_INFO_FINISHED = "ly.loud.loudly.post.info.finished";


    private static Loudly context;
    private KeyKeeper[] keyKeepers;
    private LinkedList<Post> posts;

    /**
     * @param network ID of the network
     * @return KeyKeeper or null
     */
    public KeyKeeper getKeyKeeper(int network) {
        return keyKeepers[network];
    }

    /**
     * @param network ID of the network
     * @param keyKeeper KeyKeeper, that should be stored
     */
    public void setKeyKeeper(int network, KeyKeeper keyKeeper) {
        keyKeepers[network] = keyKeeper;
    }

    /**
     * Get context of the Application.
     * As the application can't die until user kills it, it's possible to store the context here
     * @return link to the Loudly
     */
    public static Loudly getContext() {
        return context;
    }

    public void addPost(Post post) {
        posts.add(0, post);
    }

    public void addPosts(LinkedList<Post> others) {
        posts.addAll(others);
    }

    public LinkedList<Post> getPosts() {
        return posts;
    }

    public Wrap[] getWraps() {
        ArrayList<Wrap> list = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (keyKeepers[i] != null) {
                list.add(Wrap.makeWrap(i));
            }
        }
        return list.toArray(new Wrap[]{});
    }

    @Override
    public void onCreate() {
        super.onCreate();
        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;
        posts = new LinkedList<>();

        Tasks.LoadKeysTask loadKeys = new Tasks.LoadKeysTask();
        loadKeys.execute();

        Tasks.LoadPostsTask loadPosts = new Tasks.LoadPostsTask(-1, 0);
        loadPosts.execute();
    }
}
