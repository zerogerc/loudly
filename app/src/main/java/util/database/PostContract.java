package util.database;

import android.provider.BaseColumns;

public final class PostContract {
    public PostContract() {}

    public static abstract class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_LINKS = "links";
        public static final String COLUMN_NAME_FIRST_ATTACHMENT = "attachments";
        public static final String COLUMN_NAME_LOCATION = "locations";
    }
}
