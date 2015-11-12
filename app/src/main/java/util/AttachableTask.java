package util;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * AttachableTask is AsyncTask, which can attach to current activity.
 * @param <Params> Parameters, passed to doInBackground.
 * @param <Progress> Type of progress
 * @param <Result> Type of result, passed to ExecuteInUI
 */

public abstract class AttachableTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Activity activity;

    /**
     * @param activity current activity
     */
    public AttachableTask(Activity activity) {
        super();
        this.activity = activity;
    }

    /**
     * Attach task to current activity
     * @param activity link to current activity. Don't forget to detach, when activity destroys
     */
    public void attachActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Detaches task from any activity. Important to call in onDestroy
     */
    public void detachActivity() {
        activity = null;
    }

    /**
     * Function, that will be execute in UI thread after executing of AttachableTask
     * @param activity current activity
     * @param result result of AttachableTask
     */
    public abstract void ExecuteInUI(Activity activity, Result result);

    @Override
    protected void onPostExecute(Result result) {
        ExecuteInUI(activity, result);
    }
}
