package ly.loud.loudly.util.database;

import android.provider.BaseColumns;

import ly.loud.loudly.base.Networks;

public final class LinksContract  {
    public LinksContract() {}

    public static abstract class LinksEntry implements BaseColumns {
        public static final String TABLE_NAME = "links";
        public static String getColumnName(int network) {
            return Networks.nameOfNetwork(network);
        }
        public static final String COLUMN_NAME_LOUDLY = "loudly"; // Just for sameness
        public static final String COLUMN_NAME_FB = "fb";
        public static final String COLUMN_NAME_TWITTER = "twitter";
        public static final String COLUMN_NAME_INSTAGRAM = "instagram";
        public static final String COLUMN_NAME_VK = "vk";
        public static final String COLUMN_NAME_OK = "ok";
        public static final String COLUMN_NAME_MAILRU = "mailru";

        // SHOULD BE IN THE SAME ORDER AS NETWORKS IN THE "Networks" CLASS
        public static final String[] LINK_COLUMNS = {
                COLUMN_NAME_LOUDLY,
                COLUMN_NAME_FB,
                COLUMN_NAME_TWITTER,
                COLUMN_NAME_INSTAGRAM,
                COLUMN_NAME_VK,
                COLUMN_NAME_OK,
                COLUMN_NAME_MAILRU};
    }
}
