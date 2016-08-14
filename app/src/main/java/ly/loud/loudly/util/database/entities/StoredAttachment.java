package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Object that represent Attachment table
 *
 * @author Danil Kolikov
 */
@StorIOSQLiteType(table = StoredAttachment.Contract.TABLE_NAME)
public class StoredAttachment {
    @Nullable
    @StorIOSQLiteColumn(name = Contract._ID, key = true)
    Long id;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_POST_ID)
    long postId;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_TYPE)
    int type;
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_LINKS)
    long linksId;
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_EXTRA)
    String extra;

    public StoredAttachment() {
    }

    @NonNull
    public static Query selectByPostId(long postId) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_POST_ID + " = ?")
                .whereArgs(postId)
                .build();
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

    public StoredAttachment setId(@Nullable Long id) {
        this.id = id;
        return this;
    }

    public long getPostId() {
        return postId;
    }

    public StoredAttachment setPostId(long postId) {
        this.postId = postId;
        return this;
    }

    public int getType() {
        return type;
    }

    public StoredAttachment setType(int type) {
        this.type = type;
        return this;
    }

    public String getExtra() {
        return extra;
    }

    public StoredAttachment setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public long getLinksId() {
        return linksId;
    }

    public StoredAttachment setLinksId(long linksId) {
        this.linksId = linksId;
        return this;
    }

    public interface Contract extends BaseColumns {
        String TABLE_NAME = "attachments";

        String COLUMN_NAME_POST_ID = "post_id";
        String COLUMN_NAME_TYPE = "type";
        String COLUMN_NAME_LINKS = "links";
        String COLUMN_NAME_EXTRA = "extra";

        String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_NAME_POST_ID + " INTEGER, "
                        + COLUMN_NAME_TYPE + " INTEGER, "
                        + COLUMN_NAME_LINKS + " INTEGER, "
                        + COLUMN_NAME_EXTRA + " TEXT )";
    }
}
