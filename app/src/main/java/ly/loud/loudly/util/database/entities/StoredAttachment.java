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

import java.util.List;

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

    public StoredAttachment(@Nullable Long id, long postId, int type, long linksId, String extra) {
        this.id = id;
        this.postId = postId;
        this.type = type;
        this.linksId = linksId;
        this.extra = extra;
    }

    /**
     * Select attachment from DB by it's ID
     *
     * @param id       ID pf attachment
     * @param database Posts database
     * @return Attachment, or null, if not found
     */
    @Nullable
    public static StoredAttachment selectById(long id, StorIOSQLite database) {
        return database.get()
                .object(StoredAttachment.class)
                .withQuery(Query.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract._ID + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    /**
     * Delete attachment from DB by it's ID
     *
     * @param id       ID pf attachment
     * @param database Posts database
     * @return Result of deletion
     */
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

    /**
     * Select all attachments connected to post
     *
     * @param postId   ID of post
     * @param database Posts database
     * @return List of connected attachments
     */
    @NonNull
    public static List<StoredAttachment> selectByPostId(long postId, StorIOSQLite database) {
        return database.get()
                .listOfObjects(StoredAttachment.class)
                .withQuery(Query.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract.COLUMN_NAME_POST_ID + " = ?")
                        .whereArgs(postId)
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

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public long getLinksId() {
        return linksId;
    }

    public void setLinksId(long linksId) {
        this.linksId = linksId;
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

        String[] COLUMNS = {
                COLUMN_NAME_POST_ID,
                COLUMN_NAME_TYPE,
                COLUMN_NAME_LINKS,
                COLUMN_NAME_EXTRA
        };
    }
}
