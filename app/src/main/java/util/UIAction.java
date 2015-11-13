package util;

import android.app.Activity;

/**
 * Action that should know current context
 */
public interface UIAction {
    void execute(Activity activity, Object... params);
}
