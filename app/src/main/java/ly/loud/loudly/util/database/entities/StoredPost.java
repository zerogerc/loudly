package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import ly.loud.loudly.util.TimeInterval;

/**
 * Object that represents Post table
 *
 * @author Danil Kolikov
 */
@StorIOSQLiteType(table = StoredPost.Contract.TABLE_NAME)
public class StoredPost {
    @Nullable
    @StorIOSQLiteColumn(name = Contract._ID, key = true)
    Long id;

    @Nullable
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_TEXT)
    String text;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_DATE)
    long date;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_LINKS)
    long linksId;

    @Nullable
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_LOCATION)
    Long locationId;

    public StoredPost() {
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

    @NonNull
    public static Query selectByTimeInterval(@NonNull TimeInterval interval) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where("? < " + Contract.COLUMN_NAME_DATE + " and " + Contract.COLUMN_NAME_DATE + " < ?")
                .whereArgs(interval.from, interval.to)
                .orderBy(Contract._ID + " DESC")
                .build();
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public StoredPost setId(@Nullable Long id) {
        this.id = id;
        return this;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public StoredPost setText(@Nullable String text) {
        this.text = text;
        return this;
    }

    public long getDate() {
        return date;
    }

    public StoredPost setDate(long date) {
        this.date = date;
        return this;
    }

    public long getLinksId() {
        return linksId;
    }

    public StoredPost setLinksId(long linksId) {
        this.linksId = linksId;
        return this;
    }

    @Nullable
    public Long getLocationId() {
        return locationId;
    }

    public StoredPost setLocationId(@Nullable Long locationId) {
        this.locationId = locationId;
        return this;
    }

    public interface Contract extends BaseColumns {
        String TABLE_NAME = "posts";

        String COLUMN_NAME_TEXT = "text";
        String COLUMN_NAME_DATE = "date";
        String COLUMN_NAME_LINKS = "links";
        String COLUMN_NAME_LOCATION = "locations";

        String SQL_CREATE_POST_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_NAME_TEXT + " TEXT, "
                        + COLUMN_NAME_LINKS + " INTEGER, "
                        + COLUMN_NAME_DATE + " LONG, "
                        + COLUMN_NAME_LOCATION + " INTEGER )";
    }
}
