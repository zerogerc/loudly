package ly.loud.loudly.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;

import ly.loud.loudly.application.Loudly;

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

public abstract class AttachableReceiver<T extends Context> extends BroadcastReceiver {
    private WeakReference<T> context;
    private Intent lastMessage;
    private boolean stopped;

    public abstract void onMessageReceive(T context, Intent message);

    /**
     * Constructor from initial context and list of filters
     * @param context initial context
     * @param filters filters, such as Loudly.POST_FINISHED
     */
    public AttachableReceiver(T context, String... filters) {
        super();
        this.context = new WeakReference<>(context);
        IntentFilter intentFilter = new IntentFilter();
        for (String filter : filters) {
            intentFilter.addAction(filter);
        }
        lastMessage = null;
        LocalBroadcastManager.getInstance(Loudly.getContext()).registerReceiver(this, intentFilter);
        stopped = false;
    }

    /**
     * Attaches receiver to the context. If receiver stores message inside, onMessageReceive
     * will be called
     * @param context Current context
     */
    public void attach(T context) {
        this.context = new WeakReference<>(context);
        if (lastMessage != null) {
            onMessageReceive(context, lastMessage);
            lastMessage = null;
        }
    }

    /**
     * Detaches receiver from current activity
     */
    public void detach() {
        this.context.clear();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (stopped) {
            return;
        }
        if (this.context.get() == null) {
            lastMessage = intent;
        } else {
            onMessageReceive(this.context.get(), intent);
        }
    }

    /**
     * Stops receiver. Should be called at least once
     */
    public void stop() {
        LocalBroadcastManager.getInstance(Loudly.getContext()).unregisterReceiver(this);
        stopped = true;
    }
}
