package util;

import android.content.Context;

/**
 * Action that should know current context
 */
public interface UIAction {
    void execute(Context context, Object... params);
}
