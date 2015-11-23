package util;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import ly.loud.loudly.Loudly;

public abstract class BroadcastSendingTask<Param> extends AsyncTask<Param, Intent, Intent> {
    public static final String ID_FIELD = "id";
    public static final String SUCCESS_FIELD = "success";
    public static final String PROGRESS_FIELD = "progress";
    public static final String ERROR_FIELD = "error";
    public static final String NETWORK_FIELD = "network";

    public static Intent makeMessage(String action, long id) {
        Intent message = new Intent(action);
        message.putExtra(ID_FIELD, id);
        return message;
    }

    public static Intent makeError(String action, long id, String error) {
        Intent message = makeMessage(action, id);
        message.putExtra(SUCCESS_FIELD, false);
        message.putExtra(ERROR_FIELD, error);
        return message;
    }

    public static Intent makeSuccess(String action, long id) {
        Intent message = makeMessage(action, id);
        message.putExtra(SUCCESS_FIELD, true);
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
