package util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import ly.loud.loudly.Loudly;

public abstract class AttachableReceiver extends BroadcastReceiver {
    private Context context;

    public AttachableReceiver(Context context, String... filters) {
        super();
        this.context = context;
        IntentFilter intentFilter = new IntentFilter();
        for (String filter : filters) {
            intentFilter.addAction(filter);
        }
        LocalBroadcastManager.getInstance(Loudly.getContext()).registerReceiver(this, intentFilter);
    }

    public void attach(Context context) {
        this.context = context;
    }

    public void detach() {
        this.context = null;
    }

    protected Context getContext() {
        return context;
    }

    public void stop() {
        LocalBroadcastManager.getInstance(Loudly.getContext()).unregisterReceiver(this);
    }
}
