package ly.loud.loudly;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import base.KeyKeeper;
import base.Networks;
import base.Post;
import base.Tasks;
import base.Wrap;
import util.IDInterval;
import util.TimeInterval;

/**
 * Core application of Loudly app
 * Stores run-time variables
 */
public class Loudly extends Application {
    public static final int GET_INFO_INTERVAL = 30;

    private static Loudly context;
    private KeyKeeper[] keyKeepers;
    private LinkedList<Post> posts;
    private long postFromOtherNetworks = 0;

    private IDInterval[] loadedPosts;
    private int[] offsets;
    private TimeInterval timeInterval;

    private AlarmManager alarmManager;
    private PendingIntent getInfoService;

    /**
     * @param network ID of the network
     * @return KeyKeeper or null
     */
    public KeyKeeper getKeyKeeper(int network) {
        return keyKeepers[network];
    }

    /**
     * @param network   ID of the network
     * @param keyKeeper KeyKeeper, that should be stored
     */
    public void setKeyKeeper(int network, KeyKeeper keyKeeper) {
        keyKeepers[network] = keyKeeper;
    }

    /**
     * Get context of the Application.
     * As the application can't die until user kills it, it's possible to store the context here
     *
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

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public IDInterval getPostInterval(int network) {
        return loadedPosts[network];
    }

    public void setPostInterval(int network, IDInterval interval) {
        loadedPosts[network] = interval;
    }

    public long makeLocalIDForOtherNetworks() {
        return -(++postFromOtherNetworks);
    }

    public int getOffset(int network) {
        return offsets[network];
    }

    public void setOffset(int network, int offset) {
        offsets[network] = offset;
    }

    public void startGetInfoService() {
        Intent runService = new Intent(context, GetInfoService.class);
        getInfoService = PendingIntent.getService(context, 0, runService, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, GET_INFO_INTERVAL);
        alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), getInfoService);
    }

    public void stopGetInfoService() {
        alarmManager.cancel(getInfoService);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;
        posts = new LinkedList<>();
        loadedPosts = new IDInterval[Networks.NETWORK_COUNT];
        offsets = new int[Networks.NETWORK_COUNT];

        // Load it from preferences
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, -1);
        timeInterval = new TimeInterval(cal.getTimeInMillis() / 1000, -1l);

        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Tasks.LoadKeysTask loadKeys = new Tasks.LoadKeysTask();
        loadKeys.execute();
    }
}
