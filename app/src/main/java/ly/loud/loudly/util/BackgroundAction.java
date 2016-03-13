package ly.loud.loudly.util;

/**
 * Action, that can be executed without context
 */
public interface BackgroundAction {
    void execute(Object... params);
}
