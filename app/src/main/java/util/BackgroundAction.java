package util;

/**
 * Action, that can be executed without context
 * @param <Result> Type of result
 */
public interface BackgroundAction<Result> {
    Result execute(Object... params);
}
