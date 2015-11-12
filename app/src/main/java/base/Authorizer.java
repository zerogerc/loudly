package base;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import ly.loud.loudly.AuthActivity;
import util.Action;
import util.Query;

/**
 * Base of Authorizer classes
 * Classes, which extends it, should contain CREATOR as element of the implementation of Parcelable
 **/

public abstract class Authorizer implements Parcelable {
    /**
     * @return number of proper social network
     */
    public abstract int network();

    /**
     * Initial steps of authorization before opening authActivity
     * @return keys that we use to interact with social network
     */
    protected abstract KeyKeeper beginAuthorize();

    /**
     * Last step of authorization.
     * It's important to put Wrap into wrapHolder before onSuccess method invoked.
     * @param url response from authorization server
     * @param inKeys keys returned from beginAuthorize
     * @return Action that will be executed in UIThread
     */
    public abstract Action continueAuthorization(final String url, KeyKeeper inKeys);

    /**
     * @return parameters that will be send to server in order to get proper tokens
     */
    protected abstract Query getAuthQuery();

    /**
     * Check validity of response
     * @param url URL, opened in AuthActivity
     * @return true, if opened URL is valid response from server
     */
    public abstract boolean isResponse(String url);

    /**
     * Static Authorization class, which holds Authorizer.
     * It performs initial steps of authorisation and opens AuthActivity to get authorisation tokens
     */
    private static class AuthorizationTask extends AsyncTask<Object, Void, KeyKeeper> {
        private Authorizer authorizer;
        private Activity activity;

        public AuthorizationTask(Activity activity, Authorizer authorizer) {
            this.activity = activity;
            this.authorizer = authorizer;
        }

        public void attach(Activity activity) {
            this.activity = activity;
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
            Intent openWeb = new Intent(activity, AuthActivity.class);
            openWeb.putExtra("URL", authorizer.getAuthQuery().toURL());
            openWeb.putExtra("KEYS", keys);
            openWeb.putExtra("AUTHORIZER", authorizer);
            activity.startActivity(openWeb);
        }
    }

    /**
     * Creates AsyncTask, which performs initialisation and opens AuthActivity.
     * In AuthActivity after receiving password starts another async task to parse response.
     * @return AsyncTask, which authorises user in social network
     */
    public AsyncTask<Object, Void, KeyKeeper> createAsyncTask(Activity activity) {
        return new AuthorizationTask(activity, this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
