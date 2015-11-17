package util.database;

import android.provider.BaseColumns;

public final class LinksContract  {
    public LinksContract() {}

    public static abstract class LinksEntry implements BaseColumns {
        public static final String TABLE_NAME = "links";
        public static final String COLUMN_NAME_FB = "fb";
        public static final String COLUMN_NAME_TWITTER = "twitter";
        public static final String COLUMN_NAME_INSTAGRAM = "instagram";
        public static final String COLUMN_NAME_VK = "vk";
        public static final String COLUMN_NAME_OK = "ok";
        public static final String COLUMN_NAME_MAILRU = "mailru";
    }
}
