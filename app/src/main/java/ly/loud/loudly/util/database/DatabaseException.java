package ly.loud.loudly.util.database;

import java.io.IOException;

/**
 * Exception for Database errors
 */
public class DatabaseException extends IOException {
    public DatabaseException(String detailMessage) {
        super(detailMessage);
    }
}
