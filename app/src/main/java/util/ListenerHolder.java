package util;

import android.content.Context;

import base.Networks;

/**
 * Deprecated class, should be remade as part of Loudly Application
 */
public class ListenerHolder {
    private static ResponseListener[] listeners = new ResponseListener[Networks.NETWORK_COUNT];
    private static int listenerCount = 0;
    private static UIAction onFinish = null;


    public static void startSession(int listenerCount, UIAction onFinish) {
        ListenerHolder.listenerCount = listenerCount;
        ListenerHolder.onFinish = onFinish;
    }

    public static ResponseListener getListener(int network) {
        return listeners[network];
    }

    private static final Object lock = new Object();

    public static void setListener(final int network, final ResponseListener listener) {
        if (listener == null) {
            listeners[network] = null;
            return;
        }
        listeners[network] = new ResponseListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                listener.onSuccess(context, result);
                synchronized (lock) {
                    listenerCount--;
                }
                if (listenerCount == 0) {
                    onFinish.execute(context);
                    onFinish = null;
                }
                setListener(network, null);
            }

            @Override
            public void onFail(Context context, String error) {
                listener.onFail(context, error);
                synchronized (lock) {
                    listenerCount--;
                }
                if (listenerCount == 0) {
                    onFinish.execute(context);
                    onFinish = null;
                }
                setListener(network, null);
            }
        };
    }
}
