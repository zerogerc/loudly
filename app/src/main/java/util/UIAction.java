package util;

import android.content.Context;

/**
 * Action that should know current context
 */
public interface UIAction<T extends Context> {
    void execute(T context, Object... params);
}
