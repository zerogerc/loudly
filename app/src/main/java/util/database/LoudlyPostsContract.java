package util.database;

import android.provider.BaseColumns;

public final class LoudlyPostsContract {
    public LoudlyPostsContract() {}

    public static abstract class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_INFOS = "infos";
        public static final String COLUMN_NAME_ATTACHMENTS = "attachments";

    }
}
