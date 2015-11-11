package util;

import android.app.Activity;

import base.Networks;

public class ListenerHolder {
    private static ResponseListener[] listeners = new ResponseListener[Networks.NETWORK_COUNT];
    private static int listenerCount = 0;
    private static Action onFinish = null;


    public static void startSession(int listenerCount, Action onFinish) {
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
            public void onSuccess(Activity activity, Object result) {
                listener.onSuccess(activity, result);
                synchronized (lock) {
                    listenerCount--;
                }
                if (listenerCount == 0) {
                    onFinish.execute(activity);
                    onFinish = null;
                }
                setListener(network, null);
            }

            @Override
            public void onFail(Activity activity, String error) {
                listener.onFail(activity, error);
                synchronized (lock) {
                    listenerCount--;
                }
                if (listenerCount == 0) {
                    onFinish.execute(activity);
                    onFinish = null;
                }
                setListener(network, null);
            }
        };
    }
}
