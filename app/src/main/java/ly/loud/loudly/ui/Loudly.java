package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.networks.Loudly.LoudlyKeyKeeper;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.database.DatabaseActions;
import ly.loud.loudly.util.database.DatabaseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.*;

/**
 * Core application of Loudly app
 * Stores run-time variables
 */
public class Loudly extends Application {
    private static final String PREFERENCES = "loudlyprefs";
    private static final String UPDATE_FREQUENCY = "upfreq";
    private static final String LOAD_LAST = "loadlast";
    private static final int THREADS_COUNT = 6;

    private static Loudly context;
    private static Activity activity;

    private static int loadLast, getInfoInterval;
    private static ExecutorService executor = null;
    private static PostsHolder posts;
    private KeyKeeper[] keyKeepers;
    private TimeInterval timeInterval;
    private AlarmManager alarmManager;
    private PendingIntent getInfoService;

    /**
     * Load state of application from database. <br>
     * Method loads keys and preferences
     *
     * @throws DatabaseException If error with database occured
     */
    public static void loadFromDB() throws DatabaseException {
        DatabaseActions.loadKeys();

        // Loading preferences
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        int frequency = preferences.getInt(UPDATE_FREQUENCY, 30);
        int loadLast = preferences.getInt(LOAD_LAST, 7);
        setPreferences(frequency, loadLast);
        Loudly.getContext().setKeyKeeper(Networks.LOUDLY, new LoudlyKeyKeeper());
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

    /**
     * Store current visible activity into Loudly
     *
     * @param activity current activity
     */
    public synchronized static void setCurrentActivity(Activity activity) {
        Loudly.activity = activity;
    }

    /**
     * Reset stored visible activity
     *
     * @param old The stored activity
     */
    public synchronized static void clearCurrentActivity(Activity old) {
        if (Loudly.activity == old) {
            Loudly.activity = null;
        }
    }

    /**
     * Get current visible activity
     *
     * @return Activity with which user interacts
     */
    public synchronized static Activity getCurrentActivity() {
        return Loudly.activity;
    }

    public static PostsHolder getPostHolder() {
        if (posts == null) {
            posts = new PostsHolder();
        }
        return posts;
    }

    // ToDo: make singleton
    public static ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(THREADS_COUNT);
        }
        return executor;
    }

    public static void sendLocalBroadcast(Intent message) {
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(message);
    }

    private void resetState() {
        for (Wrap w : getWraps()) {
            w.resetState();
        }
        getPostHolder().clear();
        Arrays.fill(MainActivity.loadedNetworks, false);
    }

    private static void setPreferences(int updateFreq, int loadLast) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -loadLast);
        Loudly.getContext().timeInterval = new TimeInterval(calendar.getTimeInMillis() / 1000, -1L);

        if (Loudly.loadLast != loadLast) {
            // ToDo: fix this crutch
            Loudly.getContext().resetState();
        }
        Loudly.loadLast = loadLast;
        Loudly.getInfoInterval = updateFreq;
    }


    /**
     * @return Frequency of updates and posts interval
     */
    public static int[] getPreferences() {
        return new int[]{getInfoInterval, loadLast};
    }

    static void savePreferences(int frequency, int loadLast) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(UPDATE_FREQUENCY, frequency);
        editor.putInt(LOAD_LAST, loadLast);
        editor.apply();
        setPreferences(frequency, loadLast);
    }

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

    public Wrap[] getWraps() {
        ArrayList<Wrap> list = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (keyKeepers[i] != null) {
                list.add(Networks.makeWrap(i));
            }
        }
        return list.toArray(new Wrap[list.size()]);
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void startGetInfoService() {
        if (getInfoService != null) {
            stopGetInfoService();
        }
        Intent runService = new Intent(context, GetInfoService.class);
        getInfoService = PendingIntent.getService(context, 0, runService, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, getInfoInterval);
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

        try {
            loadFromDB();
        } catch (DatabaseException e) {
            // ToDo: Inform user about the error in database
        }

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        stopGetInfoService();
        posts.getPosts().clear();
    }
}
