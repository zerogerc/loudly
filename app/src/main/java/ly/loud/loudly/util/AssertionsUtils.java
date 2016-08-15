package ly.loud.loudly.util;

import android.app.Activity;
import android.support.annotation.NonNull;

public class AssertionsUtils {
    public static void assertActivityImplementsInterface(@NonNull Activity activity, @NonNull Class interfaceClass) {
            if (!interfaceClass.isAssignableFrom(activity.getClass())) {
                throw new IllegalStateException("Activity " + activity.getClass().getSimpleName() + " must implement " + interfaceClass.getSimpleName());
            }
    }
}
