package base;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ly.loud.loudly.Loudly;
import util.AttachableTask;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.Query;
import util.UIAction;
import util.database.DatabaseActions;
import util.database.DatabaseException;

/**
 * Base of Authorizer classes
 * Classes, which extends it, should contain CREATOR as element of the implementation of Parcelable
 **/

public abstract class Authorizer {
    /**
     * @return ID of proper social network
     */
    public abstract int network();

    /**
     * Initial steps of authorization before opening authActivity
     *
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
     *
     * @param keys     Keys, generated during beginAuthorize
     * @param response Response from server
     */
    public abstract void addFieldsFromQuery(KeyKeeper keys, Query response);


    /**
     * @return parameters that will be send to server in order to get proper tokens
     */
    public abstract Query makeAuthQuery();

    /**
     * Check validity of response
     *
     * @param url URL, opened in AuthFragment
     * @return true, if opened URL is valid response from server
     */
    public abstract boolean isResponse(String url);

    /**
     * Static Authorization task, which holds Authorizer.
     * It performs initial steps of authorisation and opens AuthFragment to get authorisation tokens
     */

    public static class AbstractAuthorizationTask<T extends Context> extends AttachableTask<T, Object, Void, KeyKeeper> {
        private Authorizer authorizer;
        private UIAction<T> doInUI;

        public AbstractAuthorizationTask(T context, Authorizer authorizer, UIAction<T> doInUI) {
            super(context);
            this.authorizer = authorizer;
            this.doInUI = doInUI;
        }

        @Override
        protected KeyKeeper doInBackground(Object... params) {
            Log.i("AUTHORIZER", "Async task started");
            return authorizer.beginAuthorize();
        }

        @Override
        public void executeInUI(T context, KeyKeeper result) {
            if (result == null) {
                return;
            }
            doInUI.execute(context, authorizer, result);
        }
    }

    /**
     * Create Async task, that begins authorisation, than executes UI action with initialised keyKeeper.
     * After it should be called continueAuthorisation with url of with authToken.
     *
     * @param context context to which task should be attached
     * @param doInUI  UI action, first argument - context, second - authorizer, third - keykeeper
     * @return Attachable task
     */
    public <T extends Context> AbstractAuthorizationTask<T> createAsyncTask(T context, UIAction<T> doInUI) {
        return new AbstractAuthorizationTask<>(context, this, doInUI);
    }

    public class FinishAuthorization extends BroadcastSendingTask {
        private KeyKeeper keyKeeper;
        private String url;

        public FinishAuthorization(KeyKeeper keyKeeper, String url) {
            this.keyKeeper = keyKeeper;
            this.url = url;
        }

        @Override
        protected Intent doInBackground(Object... params) {
            Query response = Query.fromURL(url);

            if (response == null) {
                return makeError(Broadcasts.AUTHORIZATION, Broadcasts.AUTH_FAIL,
                        "Failed to parse response");
            }

            if (response.containsParameter(successToken())) {
                addFieldsFromQuery(keyKeeper, response);

                Loudly.getContext().setKeyKeeper(network(), keyKeeper);

                try {
                    DatabaseActions.updateKey(network(), Loudly.getContext().getKeyKeeper(network()));
                } catch (DatabaseException e) {
                    Loudly.getContext().setKeyKeeper(network(), null);
                    return makeError(Broadcasts.AUTHORIZATION,
                            Broadcasts.DATABASE_ERROR, e.getMessage());
                }

                Intent message = BroadcastSendingTask.makeSuccess(Broadcasts.AUTHORIZATION);
                message.putExtra(Broadcasts.NETWORK_FIELD, network());

                return message;
            } else {
                String errorToken = response.getParameter(errorToken());
                return makeError(Broadcasts.AUTHORIZATION, Broadcasts.AUTH_FAIL,
                        errorToken);
            }
        }
    }

    public BroadcastSendingTask createFinishAuthorizationTask(KeyKeeper keyKeeper, String url) {
        return new FinishAuthorization(keyKeeper, url);
    }
}
