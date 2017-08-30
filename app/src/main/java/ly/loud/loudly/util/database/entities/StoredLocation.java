package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Object that represents StoredLocation table
 *
 * @author Danil Kolikov
 */
@StorIOSQLiteType(table = StoredLocation.Contract.TABLE_NAME)
public class StoredLocation {
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

    public StoredLocation() {
    }

    public StoredLocation(@Nullable Long id, @Nullable String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    public static Query selectById(long id) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract._ID + " = ?")
                .whereArgs(id)
                .build();
    }

    @NonNull
    public static DeleteQuery deleteById(long id) {
        return DeleteQuery.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract._ID + " = ?")
                .whereArgs(id)
                .build();
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
    }
}
