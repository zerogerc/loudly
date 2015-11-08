package util;

import base.Action;
import base.Networks;
import base.ResponseListener;

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

    public static void setListener(int network, final ResponseListener listener) {
        listeners[network] = new ResponseListener() {
            @Override
            public void onSuccess(Object result) {
                listener.onSuccess(result);
                synchronized (lock) {
                    listenerCount--;
                }
                if (listenerCount == 0) {
                    onFinish.execute();
                    onFinish = null;
                }
            }

            @Override
            public void onFail(String error) {
                listener.onFail(error);
                synchronized (lock) {
                    listenerCount--;
                }
                if (listenerCount == 0) {
                    onFinish.execute();
                    onFinish = null;
                }
            }
        };
    }
}
