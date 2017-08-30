package ly.loud.loudly.base.exceptions;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Fatal exception which can't be handled automatically
 */
public class FatalException extends IOException {
    public FatalException() {
        super();
    }

    public FatalException(@NonNull String detailMessage) {
        super(detailMessage);
    }

    public FatalException(@NonNull Throwable cause) {
        super(cause);
    }
}
