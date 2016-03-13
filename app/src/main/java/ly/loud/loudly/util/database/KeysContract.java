package ly.loud.loudly.util.database;

import android.provider.BaseColumns;

/**
 * Created by Данил on 11/17/2015.
 */
public final class KeysContract {
    public KeysContract() {}

    public static abstract class KeysEntry implements BaseColumns{
        public static final String TABLE_NAME = "keys";
        public static final String COLUMN_NAME_NETWORK = "network";
        public static final String COLUMN_NAME_VALUE = "value";
    }
}
