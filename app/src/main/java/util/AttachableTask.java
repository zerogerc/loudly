package util;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * AttachableTask is AsyncTask, which can attach to current context.
 * @param <T> Type of context
 * @param <Params> Parameters, passed to doInBackground.
 * @param <Progress> Type of progress
 * @param <Result> Type of result, passed to executeInUI
 */

public abstract class AttachableTask<T extends Context, Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected WeakReference<T> context;
    protected Result result;

    /**
     * @param context current context
     */
    public AttachableTask(T context) {
        super();
        this.context = new WeakReference<>(context);
        result = null;
    }

    /**
     * Attach task to current context. If task was finished when task wasn't attached,
     * executeInUI will be called instead of attach
     * @param context link to current context. Don't forget to detach, if context was destroyed
     */
    public void attach(T context) {
        if (result != null) {
            executeInUI(context, result);
            result = null;
        } else {
            this.context = new WeakReference<>(context);
        }
    }

    /**
     * Function, that will be execute in UI thread after executing of AttachableTask
     * @param context current context
     * @param result result of AttachableTask
     */
    public abstract void executeInUI(T context, Result result);

    @Override
    protected void onPostExecute(Result result) {
        if (context != null) {
            executeInUI(context.get(), result);
        } else {
            this.result = result;
        }
    }
}
