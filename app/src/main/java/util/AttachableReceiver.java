package util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import ly.loud.loudly.Loudly;

/**
 * Receiver for local broadcasts.
 *
 * When it receives a message, onMessageReceive is called with current context and message.
 * It should be stored in Activity class as static variable (for saving during re-creation of
 * activity), detached in onDestroy and then attached in onCreate.
 * If it receives a message when activity is dead, receiver stores it inside itself, and then
 * when attach() is called, onMessageReceive is called with stored message.
 *
 * It's important to stop the receiver after work with it
 */

public abstract class AttachableReceiver extends BroadcastReceiver {
    private Context context;
    private Intent lastMessage;

    public abstract void onMessageReceive(Context context, Intent message);

    /**
     * Constructor from initial context and list of filters
     * @param context initial context
     * @param filters filters, such as Loudly.POST_FINISHED
     */
    public AttachableReceiver(Context context, String... filters) {
        super();
        this.context = context;
        IntentFilter intentFilter = new IntentFilter();
        for (String filter : filters) {
            intentFilter.addAction(filter);
        }
        lastMessage = null;
        LocalBroadcastManager.getInstance(Loudly.getContext()).registerReceiver(this, intentFilter);
    }

    /**
     * Attaches receiver to the context. If receiver stores message inside, onMessageReceive
     * will be called
     * @param context Current context
     */
    public void attach(Context context) {
        this.context = context;
        if (lastMessage != null) {
            onMessageReceive(context, lastMessage);
            lastMessage = null;
        }
    }

    /**
     * Detaches receiver from activity
     */
    public void detach() {
        this.context = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (this.context == null) {
            lastMessage = intent;
        } else {
            onMessageReceive(this.context, intent);
        }
    }

    /**
     * Stops receiver. Should be called at least once
     */
    public void stop() {
        LocalBroadcastManager.getInstance(Loudly.getContext()).unregisterReceiver(this);
    }
}
