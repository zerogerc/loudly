package ly.loud.loudly.util.database;

import android.provider.BaseColumns;

/**
 * Created by Данил on 11/17/2015.
 */
public final class AttachmentsContract {
    public AttachmentsContract() {
    }

    public static abstract class AttachmentsEntry implements BaseColumns {
        public static final String TABLE_NAME = "attachments";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_EXTRA = "extra";
        public static final String COLUMN_NAME_NEXT = "next";
        public static final String COLUMN_NAME_PREV = "prev";
        public static final long END_OF_LIST_VALUE = -2;

        public static final String[] ATTACHMENTS_COLUMNS = {
                COLUMN_NAME_TYPE,
                COLUMN_NAME_LINK,
                COLUMN_NAME_EXTRA,
                COLUMN_NAME_NEXT,
                COLUMN_NAME_PREV};
    }
}
