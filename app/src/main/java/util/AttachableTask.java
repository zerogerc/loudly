package util;

import android.app.Activity;
import android.os.AsyncTask;

public abstract class AttachableTask<Params, Progress> extends AsyncTask<Params, Progress, Action> {
    protected Activity activity = null;

    public AttachableTask() {
        super();
        TaskHolder.setTask(this);
    }

    public AttachableTask(Activity activity) {
        super();
        this.activity = activity;
    }

    public void attach(Activity activity) {
        this.activity = activity;
    }

    public void detach() { this.activity = null; }

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
