package base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import ly.loud.loudly.Loudly;
import util.BroadcastSendingTask;
import util.Broadcasts;
import ly.loud.loudly.SettingsActivity;
import util.AttachableTask;
import util.Query;

/**
 * Base of Authorizer classes
 * Classes, which extends it, should contain CREATOR as element of the implementation of Parcelable
 **/

public abstract class Authorizer implements Parcelable {
    /**
     * @return ID of proper social network
     */
    public abstract int network();

    /**
     * Initial steps of authorization before opening authActivity
     * @return keys that we use to interact with social network
     */
    protected abstract KeyKeeper beginAuthorize();

    /**
     * @return Token that determines that response is successful
     */
    public abstract String successToken();

    /**
     * @return Token that says that response is unsuccessful, E.G. "error"
     */
    public abstract String errorToken();

    /**
     * Add fields such as access_token from response to KeyKeeper
     * @param keys Keys, generated during beginAuthorize
     * @param response Response from server
     */
    public abstract void addFieldsFromQuery(KeyKeeper keys, Query response);


    /**
     * @return parameters that will be send to server in order to get proper tokens
     */
    protected abstract Query makeAuthQuery();

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
        public void executeInUI(Context context, KeyKeeper result) {
            if (result == null) {
                return;
            }
            SettingsActivity activity = (SettingsActivity)context;
            SettingsActivity.webViewURL = authorizer.makeAuthQuery().toURL();
            SettingsActivity.webViewKeyKeeper = result;
            SettingsActivity.webViewAuthorizer = authorizer;
            activity.startWebView();
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

    /**
     * Last step of authorization.
     * @param url response from authorization server
     * @param inKeys keys returned from beginAuthorize
     * @return message to Settings Activity
     */

    public Intent continueAuthorization(final String url, KeyKeeper inKeys) {
        Query response = Query.fromURL(url);

        if (response == null) {
            return BroadcastSendingTask.makeError(Broadcasts.AUTHORIZATION, Broadcasts.AUTH_FAIL,
                    "Failed to parse response");
        }

        if (response.containsParameter(successToken())) {
            addFieldsFromQuery(inKeys, response);

            Loudly.getContext().setKeyKeeper(network(), inKeys);

            Intent message = BroadcastSendingTask.makeSuccess(Broadcasts.AUTHORIZATION);
            message.putExtra(Broadcasts.NETWORK_FIELD, network());

            return message;
        } else {
            String errorToken = response.getParameter(errorToken());
            return BroadcastSendingTask.makeError(Broadcasts.AUTHORIZATION, Broadcasts.AUTH_FAIL,
                    errorToken);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
