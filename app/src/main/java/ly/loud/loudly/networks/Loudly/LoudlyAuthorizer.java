package ly.loud.loudly.networks.Loudly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import ly.loud.loudly.base.Authorizer;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.ui.Loudly;
import ly.loud.loudly.util.BroadcastSendingTask;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.Query;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.database.DatabaseActions;
import ly.loud.loudly.util.database.DatabaseException;

public class LoudlyAuthorizer extends Authorizer{
    @Override
    public <T extends Context> AbstractAuthorizationTask<T> createAsyncTask(T context, UIAction<T> doInUI) {
        return new AbstractAuthorizationTask<T>(context, this, doInUI) {
            @Override
            protected KeyKeeper doInBackground(Object... params) {
                return null;
            }

            @Override
            protected void onPostExecute(KeyKeeper keyKeeper) {
                createFinishAuthorizationTask(keyKeeper, null).execute();
            }
        };
    }

    @Override
    public BroadcastSendingTask createFinishAuthorizationTask(KeyKeeper keyKeeper, String url) {
        return new BroadcastSendingTask() {
            @Override
            protected Intent doInBackground(Object... params) {
                try {
                    DatabaseActions.loadKeys();

                    // Loading preferences
                    SharedPreferences preferences = Loudly.getContext().getSharedPreferences(
                            Loudly.PREFERENCES, Context.MODE_PRIVATE);
                    int frequency = preferences.getInt(Loudly.UPDATE_FREQUENCY, 30);
                    int loadLast = preferences.getInt(Loudly.LOAD_LAST, 7);
                    Loudly.setPreferences(frequency, loadLast);
                } catch (DatabaseException e) {
                    return makeError(Broadcasts.AUTHORIZATION, Broadcasts.DATABASE_ERROR,
                            e.getMessage());
                }
                Loudly.getContext().setKeyKeeper(Networks.LOUDLY, new LoudlyKeyKeeper());
                Intent message = makeSuccess(Broadcasts.AUTHORIZATION);
                message.putExtra(Broadcasts.NETWORK_FIELD, Networks.LOUDLY);
                return message;
            }
        };
    }

    @Override
    protected KeyKeeper beginAuthorize() {
        return new LoudlyKeyKeeper();
    }

    @Override
    public String errorToken() {
        return null;
    }

    @Override
    public int network() {
        return 0;
    }

    @Override
    public Query makeAuthQuery() {
        return null;
    }

    @Override
    public boolean isResponse(String url) {
        return false;
    }

    @Override
    public String successToken() {
        return null;
    }

    @Override
    public void addFieldsFromQuery(KeyKeeper keys, Query response) {
    }
}