package base;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import ly.loud.loudly.AuthActivity;
import util.ContextHolder;
import util.Query;

/*
    W - is Wrap
    K - is KeyKeeper
    SHOULD MAKE CREATOR IN SUCCESSORS
 */

public abstract class Authorizer<W extends Wrap<K>, K extends KeyKeeper> implements Parcelable{
    public abstract int network();
    protected abstract K beginAuthorize();
    public abstract Action continueAuthorization(final String url, final K keys);
    protected abstract Query getAuthQuery();
    public abstract boolean isResponse(String url);

    public AsyncTask<Object, Void, K> createAsyncTask() {
        final Authorizer copy = this;
        return new AsyncTask<Object, Void, K>() {
            @Override
            protected K doInBackground(Object... params) {
                Log.i("AUTHORIZER", "Async task started");
                return beginAuthorize();
            }

            @Override
            protected void onPostExecute(K keys) {
                super.onPostExecute(keys);
                if (keys == null) {
                    return;
                }
                Activity context = ContextHolder.getContext();
                Intent openWeb = new Intent(context, AuthActivity.class);
                openWeb.putExtra("URL", getAuthQuery().toURL());
                openWeb.putExtra("KEYS", keys);
                openWeb.putExtra("AUTHORIZER", copy);
                context.startActivity(openWeb);
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}
}
