package ly.loud.loudly.application;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

import java.util.Calendar;

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
    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private static Loudly context;

    @Deprecated
    private KeyKeeper[] keyKeepers;

    @Deprecated
    private TimeInterval timeInterval;

    @SuppressWarnings("NullableProblems") // Inject
    @NonNull
    private AppComponent appComponent;

    @SuppressWarnings("NullableProblems") // Inject
    @NonNull
    private DatabaseComponent databaseComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this);

        initInjector();

        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;

        setPreferences(700);
        try {
            // ToDo : remove
            DatabaseUtils.loadKeys();
        } catch (DatabaseException e) {
            // ToDo: Inform user about the error in database
        }
    }

    /**
     * Get context of the Application.
     * As the application can't die until user kills it, it's possible to store the context here
     *
     * @return link to the Loudly
     */
    @NonNull
    public static Loudly getContext() {
        return context;
    }

    private static void setPreferences(int loadLast) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -loadLast);
        Loudly.getContext().timeInterval = TimeInterval.since(calendar.getTimeInMillis() / 1000);
    }

    /**
     * @param network ID of the network
     * @return KeyKeeper or null
     */
    @Nullable
    @Deprecated
    public KeyKeeper getKeyKeeper(int network) {
        return keyKeepers[network];
    }

    /**
     * @param network   ID of the network
     * @param keyKeeper KeyKeeper, that should be stored
     */
    @Deprecated
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

    @Deprecated
    public TimeInterval getTimeInterval() {
        return timeInterval;
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

    @NonNull
    public AppComponent getAppComponent() {
        return appComponent;
    }

    @NonNull
    public DatabaseComponent getDatabaseComponent() {
        return databaseComponent;
    }

}
