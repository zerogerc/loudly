package ly.loud.loudly.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.Utils;

/**
 * @author Danil Kolikov
 */
public class LocalBroadcastReceiver extends BroadcastReceiver {
    public LocalBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(Broadcasts.STATUS_FIELD, -1);
        switch (status) {
            case Broadcasts.ERROR:
                int error = intent.getIntExtra(Broadcasts.ERROR_KIND, -1);
                if (error == Broadcasts.EXPIRED_TOKEN) {
                    int network = intent.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    Loudly.getContext().setKeyKeeper(network, null);

                    if (Loudly.getCurrentActivity() != null) {
                        String begin = Loudly.getContext().getResources().getString(R.string.token_expired_begin);
                        String end = Loudly.getContext().getResources().getString(R.string.token_expired_end);
                        Utils.showSnackBar(begin + " " + Networks.nameOfNetwork(network) + end);
                    }
                }
        }
    }
}
