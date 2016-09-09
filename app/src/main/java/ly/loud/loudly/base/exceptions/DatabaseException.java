package ly.loud.loudly.base.exceptions;

import java.io.IOException;

/**
 * Exception for Database errors
 */
public class DatabaseException extends FatalException {
    public DatabaseException(String detailMessage) {
        super(detailMessage);
    }
}
