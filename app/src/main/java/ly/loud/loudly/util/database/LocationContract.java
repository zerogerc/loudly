package ly.loud.loudly.util.database;

import android.provider.BaseColumns;

/**
 * Created by Данил on 11/17/2015.
 */
public final class LocationContract {
    public LocationContract() {}

    public static abstract class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_NAME = "name";

        public static final String[] LOCATION_COLUMNS = {
                COLUMN_NAME_LATITUDE,
                COLUMN_NAME_LONGITUDE,
                COLUMN_NAME_NAME};
    }
}
