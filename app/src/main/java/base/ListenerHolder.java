package base;

public class ListenerHolder {
    private static ResponseListener listener;

    public static ResponseListener getListener() {
        return listener;
    }

    public static void setListener(ResponseListener listener) {
        ListenerHolder.listener = listener;
    }
}
