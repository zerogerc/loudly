package base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import ly.loud.loudly.AuthActivity;
import util.UIAction;
import util.AttachableTask;
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
     * It's important to store KeyKeeper in Loudly before onSuccess method invoked.
     * @param url response from authorization server
     * @param inKeys keys returned from beginAuthorize
     * @return UIAction that will be executed in UIThread
     */
    public abstract UIAction continueAuthorization(final String url, KeyKeeper inKeys);

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
    private static class AuthorizationTask extends AttachableTask<Object, Void, KeyKeeper> {
        private Authorizer authorizer;

        public AuthorizationTask(Activity activity, Authorizer authorizer) {
            super(activity);
            this.authorizer = authorizer;
        }

        @Override
        protected KeyKeeper doInBackground(Object... params) {
            Log.i("AUTHORIZER", "Async task started");
            return authorizer.beginAuthorize();
        }

        @Override
        public void ExecuteInUI(Context context, KeyKeeper result) {
            if (result == null) {
                return;
            }
            Intent openWeb = new Intent(context, AuthActivity.class);
            openWeb.putExtra("URL", authorizer.getAuthQuery().toURL());
            openWeb.putExtra("KEYS", result);
            openWeb.putExtra("AUTHORIZER", authorizer);
            context.startActivity(openWeb);
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
