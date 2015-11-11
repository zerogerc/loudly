package base;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import ly.loud.loudly.AuthActivity;
import util.Action;
import util.ContextHolder;
import util.Query;

/*
    W - is Wrap
    K - is KeyKeeper
    SHOULD MAKE CREATOR IN SUCCESSORS
 */

public abstract class Authorizer implements Parcelable {
    public abstract int network();

    protected abstract KeyKeeper beginAuthorize();

    public abstract Action continueAuthorization(final String url, KeyKeeper inKeys);

    protected abstract Query getAuthQuery();

    public abstract boolean isResponse(String url);

    private static class AuthorizationTask extends AsyncTask<Object, Void, KeyKeeper> {
        Authorizer authorizer;

        public AuthorizationTask(Authorizer authorizer) {
            this.authorizer = authorizer;
        }

        @Override
        protected KeyKeeper doInBackground(Object... params) {
            Log.i("AUTHORIZER", "Async task started");
            return authorizer.beginAuthorize();
        }

        @Override
        protected void onPostExecute(KeyKeeper keys) {
            super.onPostExecute(keys);
            if (keys == null) {
                return;
            }
            Activity context = ContextHolder.getContext();
            Intent openWeb = new Intent(context, AuthActivity.class);
            openWeb.putExtra("URL", authorizer.getAuthQuery().toURL());
            openWeb.putExtra("KEYS", keys);
            openWeb.putExtra("AUTHORIZER", authorizer);
            context.startActivity(openWeb);
        }
    }

    public AsyncTask<Object, Void, KeyKeeper> createAsyncTask() {
        return new AuthorizationTask(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
