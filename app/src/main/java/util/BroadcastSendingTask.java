package util;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import ly.loud.loudly.Loudly;

public abstract class BroadcastSendingTask<Param> extends AsyncTask<Param, Intent, Intent> {
    public static Intent makeMessage(String action, String status) {
        Intent message = new Intent(action);
        message.putExtra(Broadcasts.STATUS_FIELD, status);
        return message;
    }

    public static Intent makeMessage(String action, String status, long id) {
        Intent message = makeMessage(action, status);
        message.putExtra(Broadcasts.ID_FIELD, id);
        return message;
    }

    public static Intent makeError(String action, String errorKind, String error) {
        Intent message = new Intent(action);
        message.putExtra(Broadcasts.ERROR_KIND, errorKind);
        message.putExtra(Broadcasts.ERROR_FIELD, error);
        return message;
    }

    public static Intent makeError(String action, String errorKind, long id, String error) {
        Intent message = makeError(action, errorKind, error);
        message.putExtra(Broadcasts.ID_FIELD, id);
        return message;
    }

    public static Intent makeSuccess(String action) {
        Intent message = makeMessage(action, Broadcasts.FINISHED);
        return message;
    }

    public static Intent makeSuccess(String action, long id) {
        Intent message = makeSuccess(action);
        message.putExtra(Broadcasts.ID_FIELD, id);
        return message;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        LocalBroadcastManager.getInstance(Loudly.getContext()).sendBroadcast(intent);
    }

    @Override
    protected void onProgressUpdate(Intent... values) {
        LocalBroadcastManager.getInstance(Loudly.getContext()).sendBroadcast(values[0]);
    }
}
