package ly.loud.loudly.util;

import android.content.Intent;
import android.os.AsyncTask;

import ly.loud.loudly.application.Loudly;

public abstract class BroadcastSendingTask extends AsyncTask<Object, Intent, Intent> {
    public static Intent makeMessage(String action, int status) {
        Intent message = new Intent(action);
        message.putExtra(Broadcasts.STATUS_FIELD, status);
        return message;
    }

    public static Intent makeError(String action, int errorKind, String error) {
        Intent message = new Intent(action);
        message.putExtra(Broadcasts.STATUS_FIELD, Broadcasts.ERROR);
        message.putExtra(Broadcasts.ERROR_KIND, errorKind);
        message.putExtra(Broadcasts.ERROR_FIELD, error);
        return message;
    }

    public static Intent makeSuccess(String action) {
        return makeMessage(action, Broadcasts.FINISHED);
    }

    @Override
    protected void onPostExecute(Intent intent) {
        if (intent != null) {
            Loudly.sendLocalBroadcast(intent);
        }
    }

    @Override
    protected void onProgressUpdate(Intent... values) {
        Loudly.sendLocalBroadcast(values[0]);
    }
}
