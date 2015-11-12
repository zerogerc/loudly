package util;

import android.app.Activity;

/**
 * LongTask is AttachableTask that runs, even when activity dies.
 * LongTask is stored in TaskHolder, and Activity can attach to it.
 * Then, after finish of LongTask, Action is called in the context of current activity.
 * If LongTask have finished before any activity attached to it, LongTask will store Action in TaskHolder.
 * @param <Params> Parameters, passed to doInBackground
 * @param <Progress> Type of progress
 */
public abstract class LongTask<Params, Progress> extends AttachableTask<Params, Progress, Action> {
    public LongTask() {
        super(null);
        TaskHolder.setTask(this);
    }

    @Override
    public void ExecuteInUI(Activity activity, Action result) {}

    @Override
    protected void onPostExecute(Action action) {
        if (activity == null) {
            TaskHolder.setAction(action);
        } else {
            action.execute(activity);
        }
        TaskHolder.setTask(null);
    }
}
