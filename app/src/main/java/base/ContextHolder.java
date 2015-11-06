package base;

import android.app.Activity;

/**
 * Created by ZeRoGerc on 06.11.15.
 */
public class ContextHolder {
    private static Activity context;
    public static void setContext(Activity activity) {
        context = activity;
    }
    public static Activity getContext() {
        return context;
    }
}
