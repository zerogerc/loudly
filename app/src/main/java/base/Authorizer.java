package base;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

/*
    W - is Wrap
    K - is KeyKeeper
 */
public abstract class Authorizer<W extends Wrap<K>, K extends KeyKeeper> {
    protected ResponseListener<? extends Activity, W> listener = null;

    protected abstract K beginAuthorize();
    protected abstract void continueAuthorization(String url, K keys);
    protected abstract String getAuthUrl();

    public Authorizer() {}

    public Authorizer(ResponseListener<? extends Activity, W> listener) {
        this.listener = listener;
    }

    public AsyncTask<Object, Void, Void> createAsyncTask() {
        Log.e("TAG", "here1");
        return new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                final K keys = beginAuthorize();
                Log.e("TAG", "here");
                return null;
            }
        };
    };
}
