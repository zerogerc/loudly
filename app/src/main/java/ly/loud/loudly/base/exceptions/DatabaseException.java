package ly.loud.loudly.base.exceptions;

import android.support.annotation.NonNull;

/**
 * Exception for Database errors
 */
public class DatabaseException extends FatalException {
    public DatabaseException(@NonNull String detailMessage) {
        super(detailMessage);
    }
}
