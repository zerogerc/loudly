package ly.loud.loudly.application;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ly.loud.loudly.legacy_base.Wrap;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.database.DaggerDatabaseComponent;
import ly.loud.loudly.util.database.DatabaseComponent;
import ly.loud.loudly.util.database.DatabaseException;
import ly.loud.loudly.util.database.DatabaseUtils;
import ly.loud.loudly.util.database.KeysDbModule;
import ly.loud.loudly.util.database.PostDbModule;

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
    private KeyKeeper[] keyKeepers;
    private TimeInterval timeInterval;
    private AlarmManager alarmManager;
    private PendingIntent getInfoService;

    private AppComponent appComponent;
    private DatabaseComponent databaseComponent;

    private static SharedPreferences preferences;

    private static OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            int frequency = preferences.getInt(UPDATE_FREQUENCY, 30);
            int loadLast1 = Integer.parseInt(preferences.getString(LOAD_LAST, "7"));
            setPreferences(frequency, loadLast1);
        }
    };


    /**
     * Load state of application from database. <br>
     * Method loads keys and preferences
     *
     * @throws DatabaseException If error with database occured
     */
    public static void loadFromDB() throws DatabaseException {
        DatabaseUtils.loadKeys();

        // Loading preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.registerOnSharedPreferenceChangeListener(listener);

        int frequency = preferences.getInt(UPDATE_FREQUENCY, 30);
        int loadLast = Integer.parseInt(preferences.getString(LOAD_LAST, "7"));
        setPreferences(frequency, loadLast);
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

    /**
     * Store current visible activity into Loudly
     *
     * @param activity current activity
     */
    public synchronized static void setCurrentActivity(@NonNull Activity activity) {
        Loudly.activity = activity;
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

    private static void setPreferences(int updateFreq, int loadLast) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, - loadLast);
        Loudly.getContext().timeInterval = TimeInterval.since(calendar.getTimeInMillis() / 1000);

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
        return new int[]{ getInfoInterval, loadLast};
    }

    private void resetState() {
        for (Wrap w : getWraps()) {
            if (w != null) {
                w.resetState();
            }
        }
    }

    /**
     * @param network ID of the network
     * @return KeyKeeper or null
     */
    @Nullable
    public KeyKeeper getKeyKeeper(int network) {
        return keyKeepers[network];
    }

    /**
     * @param network   ID of the network
     * @param keyKeeper KeyKeeper, that should be stored
     */
    public void setKeyKeeper(int network, KeyKeeper keyKeeper) {
        if (keyKeeper != null) {
            getAppComponent().coreModel().connectToNetworkById(network, keyKeeper)
                    .subscribe(aBoolean -> {
                        if (!aBoolean) {
                            Log.e("NETWORK", "Could not connect with keykeeper");
                        }
                    });
        } else {
            // TODO: disconnect
        }

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

    @Override
    public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this);

        initInjector();

        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;

        try {
            loadFromDB();
        } catch (DatabaseException e) {
            // ToDo: Inform user about the error in database
        }

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    private void initInjector() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        databaseComponent = DaggerDatabaseComponent.builder()
                .keysDbModule(new KeysDbModule())
                .postDbModule(new PostDbModule())
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public DatabaseComponent getDatabaseComponent() {
        return databaseComponent;
    }

}
