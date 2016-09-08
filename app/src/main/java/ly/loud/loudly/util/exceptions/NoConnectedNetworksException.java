package ly.loud.loudly.util.exceptions;

import android.support.annotation.NonNull;

public class NoConnectedNetworksException extends Exception {

    public NoConnectedNetworksException(@NonNull String message) {
        super(message);
    }
}
