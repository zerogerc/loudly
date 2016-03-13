package ly.loud.loudly.util.database;

import java.io.IOException;

/**
 * Created by Данил on 11/19/2015.
 */
public class DatabaseException extends IOException {
    public DatabaseException(String detailMessage) {
        super(detailMessage);
    }
}
