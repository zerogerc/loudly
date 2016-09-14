package ly.loud.loudly.base.exceptions;

import android.support.annotation.NonNull;

/**
 * Exception that rises when there is no internet connection
 */
public class NoNetworkConnectionException extends FatalException {
    public NoNetworkConnectionException() {
        super();
    }

    public NoNetworkConnectionException(@NonNull Throwable cause) {
        super(cause);
    }
}
