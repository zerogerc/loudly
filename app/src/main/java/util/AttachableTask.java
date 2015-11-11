package util;

import android.app.Activity;
import android.os.AsyncTask;

public abstract class AttachableTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    public abstract void onExecuteInUI(Activity activity, Result result);

    protected Activity activity = null;
    public void attach(Activity activity) {
        this.activity = activity;
    }
    public AttachableTask() {
        super();
        TaskHolder.setTask(this);
    }


    @Override
    protected void onPostExecute(Result result) {
        if (activity != null) {
            onExecuteInUI(activity, result);
        }
        TaskHolder.setTask(null);
    }
}
