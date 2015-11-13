package util;

public class TaskHolder {
    static LongTask task = null;
    static UIAction action = null;

    public static LongTask getTask() {
        return task;
    }

    public static void setTask(LongTask task) {
        TaskHolder.task = task;
    }

    public static UIAction getAction() {
        return action;
    }

    public static void setAction(UIAction action) {
        TaskHolder.action = action;
    }
}
