package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Object that represents Location table
 *
 * @author Danil Kolikov
 */
@StorIOSQLiteType(table = Location.Contract.TABLE_NAME)
public class Location {
    @StorIOSQLiteColumn(name = Contract._ID, key = true)
    @Nullable
    Long id;

    @Nullable
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_NAME)
    String name;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_LATITUDE)
    double latitude;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_LONGITUDE)
    double longitude;

    public Location() {
    }

    public Location(@Nullable String name, double latitude, double longitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = null;
    }

    /**
     * Select location from DB by it's ID
     *
     * @param id ID of location
     * @param database Posts database
     * @return Location (may be null if not found)
     */
    @Nullable
    public static Location selectById(Long id, StorIOSQLite database) {
        return database.get()
                .object(Location.class)
                .withQuery(Query.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract._ID + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }


    /**
     * Delete location from DB by it's ID
     *
     * @param id ID of location
     * @param database Posts database
     * @return Result of deletion
     */
    @Nullable
    public static DeleteResult deleteById(Long id, StorIOSQLite database) {
        return database
                .delete()
                .byQuery(DeleteQuery.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract._ID + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public interface Contract extends BaseColumns {
        String TABLE_NAME = "locations";
        String COLUMN_NAME_LATITUDE = "latitude";
        String COLUMN_NAME_LONGITUDE = "longitude";
        String COLUMN_NAME_NAME = "name";

        String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_NAME_LATITUDE + " REAL, "
                        + COLUMN_NAME_LONGITUDE + " REAL, "
                        + COLUMN_NAME_NAME + " TEXT )";

        String[] COLUMNS = {
                COLUMN_NAME_LATITUDE,
                COLUMN_NAME_LONGITUDE,
                COLUMN_NAME_NAME
        };
    }
}
