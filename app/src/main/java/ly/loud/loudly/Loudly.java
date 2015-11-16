package ly.loud.loudly;

import android.app.Application;

import java.util.ArrayDeque;
import java.util.LinkedList;

import base.KeyKeeper;
import base.Networks;
import base.Post;
import util.LongTask;
import util.ResultListener;
import util.UIAction;

/**
 * Core application of Loudly app
 * Stores run-time variables
 */
public class Loudly extends Application {
    private static Loudly context;
    private KeyKeeper[] keyKeepers;
    private LongTask task;
    private UIAction action;
    private ResultListener listener;
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

    public LongTask getTask() {
        return task;
    }

    public void setTask(LongTask task) {
        this.task = task;
    }

    public UIAction getAction() {
        return action;
    }

    public void setAction(UIAction action) {
        this.action = action;
    }

    public ResultListener getListener() {
        return listener;
    }

    public void setListener(ResultListener listener) {
        this.listener = listener;
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public LinkedList<Post> getPosts() {
        return posts;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;
        posts = new LinkedList<>();
    }
}
