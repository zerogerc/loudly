package ly.loud.loudly;

import android.app.Application;

import base.KeyKeeper;
import base.Networks;

/**
 * Core application of Loudly app
 * Stores run-time constants
 */
public class Loudly extends Application {
    private static Loudly context;
    private KeyKeeper[] keyKeepers;

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

    @Override
    public void onCreate() {
        super.onCreate();
        keyKeepers = new KeyKeeper[Networks.NETWORK_COUNT];
        context = this;
    }
}
