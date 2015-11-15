package util;

import android.content.Context;
import android.os.AsyncTask;

/**
 * AttachableTask is AsyncTask, which can attach to current context.
 * @param <Params> Parameters, passed to doInBackground.
 * @param <Progress> Type of progress
 * @param <Result> Type of result, passed to ExecuteInUI
 */

public abstract class AttachableTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Context context;

    /**
     * @param context current context
     */
    public AttachableTask(Context context) {
        super();
        this.context = context;
    }

    /**
     * Attach task to current context
     * @param context link to current context. Don't forget to detach, when context destroys
     */
    public void attachContext(Context context) {
        this.context = context;
    }

    /**
     * Detaches task from any context. Important to call in onDestroy
     */
    public void detachContext() {
        context = null;
    }

    /**
     * Function, that will be execute in UI thread after executing of AttachableTask
     * @param context current context
     * @param result result of AttachableTask
     */
    public abstract void ExecuteInUI(Context context, Result result);

    @Override
    protected void onPostExecute(Result result) {
        ExecuteInUI(context, result);
    }
}
