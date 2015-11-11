package util;

public class TaskHolder {
    static AttachableTask task = null;

    public static AttachableTask getTask() {
        return task;
    }

    public static void setTask(AttachableTask task) {
        TaskHolder.task = task;
    }
}
