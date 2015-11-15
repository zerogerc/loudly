package util;

import android.content.Context;

/**
 * LongTask is AttachableTask that runs, even when context dies.
 * LongTask is stored in TaskHolder, and Activity can attach to it.
 * Then, after finish of LongTask, UIAction is called in the context of current context.
 * If LongTask have finished before any context attached to it, LongTask will store UIAction in TaskHolder.
 * @param <Params> Parameters, passed to doInBackground
 * @param <Progress> Type of progress
 */
public abstract class LongTask<Params, Progress> extends AttachableTask<Params, Progress, UIAction> {
    public LongTask() {
        super(null);
        TaskHolder.setTask(this);
    }

    @Override
    public void ExecuteInUI(Context context, UIAction result) {}

    @Override
    protected void onPostExecute(UIAction action) {
        if (context == null) {
            TaskHolder.setAction(action);
        } else {
            action.execute(context);
        }
        TaskHolder.setTask(null);
    }
}
