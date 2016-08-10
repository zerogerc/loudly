package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import ly.loud.loudly.util.TimeInterval;

import java.util.Collections;
import java.util.List;

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

    public StoredPost(@Nullable Long id, @Nullable String text, long date, long linksId,
                      @Nullable Long locationId) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.linksId = linksId;
        this.locationId = locationId;
    }

    @Nullable
    public static StoredPost selectById(long id, StorIOSQLite database) {
        return database.get()
                .object(StoredPost.class)
                .withQuery(Query.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract._ID + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    @NonNull
    public static DeleteResult deleteById(long id, StorIOSQLite database) {
        return database.delete()
                .byQuery(DeleteQuery.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract._ID + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    @NonNull
    public static List<StoredPost> selectByTimeInterval(TimeInterval interval, StorIOSQLite database) {
        // ToDo: Fix it
        return Collections.emptyList();
//        return database.get()
//                .listOfObjects(StoredPost.class)
//                .withQuery(Query.builder()
//                        .table(Contract.TABLE_NAME)
//                        .where("? < " + Contract.COLUMN_NAME_DATE + " and " + Contract.COLUMN_NAME_DATE + " < ?")
//                        .whereArgs(interval.from, interval.to)
//                        .orderBy(Contract._ID + " DESC")
//                        .build())
//                .prepare()
//                .executeAsBlocking();
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getLinksId() {
        return linksId;
    }

    public void setLinksId(long linksId) {
        this.linksId = linksId;
    }

    @Nullable
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(@Nullable Long locationId) {
        this.locationId = locationId;
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

        String[] Contract = {
                _ID,
                COLUMN_NAME_TEXT,
                COLUMN_NAME_DATE,
                COLUMN_NAME_LOCATION,
                COLUMN_NAME_LINKS
        };
    }
}
