package util;

public class TaskHolder {
    static AttachableTask task = null;
    static Action action = null;

    public static AttachableTask getTask() {
        return task;
    }

    public static void setTask(AttachableTask task) {
        TaskHolder.task = task;
    }

    public static Action getAction() {
        return action;
    }

    public static void setAction(Action action) {
        TaskHolder.action = action;
    }
}
