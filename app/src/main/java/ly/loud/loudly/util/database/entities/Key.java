package ly.loud.loudly.util.database.entities;

import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

import java.util.List;

/**
 * Object that represents Key table
 *
 * @author Danil Kolikov
 */
@StorIOSQLiteType(table = Key.Contract.TABLE_NAME)
public class Key {
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_NETWORK, key = true)
    int network;

    @Nullable
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_VALUE)
    String value;

    public Key() {
    }

    public Key(int network, @Nullable String value) {
        this.network = network;
        this.value = value;
    }

    /**
     * Load all keys from DB
     *
     * @param database Keys database
     * @return List of stored keys (Keys may be null)
     */
    public static List<Key> selectKeys(StorIOSQLite database) {
        return database.get()
                .listOfObjects(Key.class)
                .withQuery(Query.builder()
                        .table(Contract.TABLE_NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    /**
     * Delete key from DB
     *
     * @param network  ID of key's network
     * @param database Keys database
     * @return Result of deletion
     */
    public static DeleteResult deleteKey(int network, StorIOSQLite database) {
        return database.delete()
                .byQuery(DeleteQuery.builder()
                        .table(Contract.TABLE_NAME)
                        .where(Contract.COLUMN_NAME_NETWORK + " = ?")
                        .whereArgs(network)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public int getNetwork() {
        return network;
    }

    public void setNetwork(int network) {
        this.network = network;
    }

    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }
//
//    @Override
//    public void store() throws DatabaseException {
//        SQLiteDatabase db = PostDbHelper.getInstance().getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(Contract.COLUMN_NAME_NETWORK, network);
//        values.put(Contract.COLUMN_NAME_VALUE, value);
//
//        long keyId = DatabaseActions.upsert(db, Contract.TABLE_NAME,
//                Contract.COLUMN_NAME_NETWORK, network, values);
//
//        if (keyId == -1) {
//            throw new DatabaseException("Can't save key for the network # " + network);
//        }
//    }
//
//    @Override
//    public void delete() throws DatabaseException {
//        SQLiteDatabase db = KeysDbHelper.getInstance().getWritableDatabase();
//        int count = db.delete(Contract.TABLE_NAME,
//                DatabaseActions.sqlEqual(Contract.COLUMN_NAME_NETWORK, network), null);
//        if (count == 0) {
//            throw new DatabaseException("Can't delete key for network: " + network);
//        }
//    }
//
//    public static List<Key> selectAll() {
//        SQLiteDatabase db = KeysDbHelper.getInstance().getReadableDatabase();
//        List<Key> loaded = new ArrayList<>();
//
//        Cursor cursor = null;
//        try {
//            String[] projection = {
//                    Contract.COLUMN_NAME_NETWORK,
//                    Contract.COLUMN_NAME_VALUE
//            };
//            cursor = db.query(Contract.TABLE_NAME, projection, null, null, null, null, null);
//            cursor.moveToFirst();
//
//            while (!cursor.isAfterLast()) {
//                int network = cursor.getInt(cursor.getColumnIndex(Contract.COLUMN_NAME_NETWORK));
//                String bundle = cursor.getString(cursor.getColumnIndex(Contract.COLUMN_NAME_VALUE));
//                loaded.add(new Key(network, bundle));
//
//                cursor.moveToNext();
//            }
//        } finally {
//            Utils.closeQuietly(cursor);
//        }
//        return loaded;
//    }

    public interface Contract extends BaseColumns {
        String TABLE_NAME = "keys";
        String COLUMN_NAME_NETWORK = "network";
        String COLUMN_NAME_VALUE = "value";

        String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_NAME_NETWORK + " INTEGER, "
                        + COLUMN_NAME_VALUE + " TEXT )";
    }
}
