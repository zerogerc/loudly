package ly.loud.loudly;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Calendar;

import base.KeyKeeper;
import base.Networks;
import base.Tasks;
import base.Wrap;
import util.TimeInterval;

/**
 * Core application of Loudly app
 * Stores run-time variables
 */
public class Loudly extends Application {
    public static final int GET_INFO_INTERVAL = 30;

    private static Loudly context;
    private KeyKeeper[] keyKeepers;
    private TimeInterval timeInterval;

    private AlarmManager alarmManager;
    PendingIntent getInfoService;

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

    public static void sendLocalBroadcast(Intent message) {
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(message);
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
        cal.add(Calendar.SECOND, GET_INFO_INTERVAL);
        alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), getInfoService);
    }

    public void stopGetInfoService() {
        GetInfoService.stop();
        alarmManager.cancel(getInfoService);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;

        // Load it from preferences
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, -2);
        timeInterval = new TimeInterval(cal.getTimeInMillis() / 1000, -1l);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Tasks.LoadKeysTask loadKeys = new Tasks.LoadKeysTask();
        loadKeys.execute();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        stopGetInfoService();
        MainActivity.posts.clear();
        MainActivity.dbLoaded = false;
    }
}
