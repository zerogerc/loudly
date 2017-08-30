package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import ly.loud.loudly.base.entities.Event.EventType;
import ly.loud.loudly.networks.Networks.Network;

@StorIOSQLiteType(table = "events")
public class StoredEvent {

    @StorIOSQLiteColumn(name = Contract._ID, key = true)
    @Nullable
    Long id;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_TYPE)
    @EventType
    short type;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_NETWORK)
    @Network
    int network;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_POST_ID)
    long postId;

    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_DATE)
    long date;

    public StoredEvent() {
    }

    public StoredEvent(@Nullable Long id,
                       @EventType short type,
                       @Network int network,
                       long postId,
                       long date) {
        this.id = id;
        this.postId = postId;
        this.type = type;
        this.network = network;
        this.date = date;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public long getPostId() {
        return postId;
    }

    @EventType
    public short getType() {
        return type;
    }

    @Network
    public int getNetwork() {
        return network;
    }

    public long getDate() {
        return date;
    }

    @NonNull
    public static Query selectAll() {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .build();
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
    public static DeleteQuery deleteByPostId(long postId) {
        return DeleteQuery.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_POST_ID + " = ?")
                .whereArgs(postId)
                .build();
    }

    @NonNull
    public static Query selectByPostIdAndType(long postId,
                                              @EventType short type) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_POST_ID + " = ? AND "
                        + Contract.COLUMN_NAME_TYPE + " = ?")
                .whereArgs(postId, type)
                .build();
    }

    @NonNull
    public static Query selectByPostIdNetworkAndType(long postId,
                                                     @Network int network,
                                                     @EventType short type) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_POST_ID + " = ? AND "
                        + Contract.COLUMN_NAME_NETWORK + " = ? AND "
                        + Contract.COLUMN_NAME_TYPE + " = ? AND ")
                .whereArgs(postId, network, type)
                .build();
    }

    @NonNull
    public static Query selectTypesByPostIdAndNetwork(long postId, @Network int network) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_POST_ID + " = ? AND " +
                        Contract.COLUMN_NAME_NETWORK + " = ?")
                .whereArgs(postId, network)
                .columns(Contract.COLUMN_NAME_TYPE)
                .build();
    }

    @NonNull
    public static Query selectTypesByPost(long postId) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_POST_ID + " = ?")
                .whereArgs(postId)
                .columns(Contract.COLUMN_NAME_TYPE)
                .build();
    }

    @NonNull
    public static Query selectOldestEvents(long postId,
                                           @EventType short type,
                                           int count) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_TYPE + " = ? AND "
                                + Contract.COLUMN_NAME_POST_ID + " = ?"
                )
                .whereArgs(type, postId)
                .orderBy(Contract.COLUMN_NAME_POST_ID + " ASC")
                .limit(count)
                .build();
    }

    public interface Contract extends BaseColumns {
        String TABLE_NAME = "events";
        String COLUMN_NAME_TYPE = "type";
        String COLUMN_NAME_POST_ID = "post_id";
        String COLUMN_NAME_NETWORK = "network";
        String COLUMN_NAME_DATE = "date";

        String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NAME_TYPE + " INTEGER, " +
                        COLUMN_NAME_POST_ID + " INTEGER, " +
                        COLUMN_NAME_NETWORK + " INTEGER, " +
                        COLUMN_NAME_DATE + " INTEGER)";
    }
}
