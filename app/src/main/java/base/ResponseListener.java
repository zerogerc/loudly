package base;

import android.app.Activity;
/*
    R - is result (Object)
 */
public abstract class ResponseListener<A extends Activity, R> {
    private A activity;

    public ResponseListener(A activity) {
        this.activity = activity;
    }

    public A getActivity() {
        return activity;
    }

    public void setActivity(A activity) {
        this.activity = activity;
    }

    public abstract void onSuccess(R result);
    public abstract void onFail(String error);
}
