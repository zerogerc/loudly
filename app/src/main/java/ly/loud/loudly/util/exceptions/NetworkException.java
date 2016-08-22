package ly.loud.loudly.util.exceptions;

import android.support.annotation.NonNull;

import java.io.IOException;

public class NetworkException extends IOException {

    public NetworkException(@NonNull String message) {
        super(message);
    }
}
