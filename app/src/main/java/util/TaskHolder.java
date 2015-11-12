package util;

public class TaskHolder {
    static LongTask task = null;
    static Action action = null;

    public static LongTask getTask() {
        return task;
    }

    public static void setTask(LongTask task) {
        TaskHolder.task = task;
    }

    public static Action getAction() {
        return action;
    }

    public static void setAction(Action action) {
        TaskHolder.action = action;
    }
}
