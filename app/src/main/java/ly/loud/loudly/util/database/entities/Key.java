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

import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks.Network;

/**
 * Object that represents Key table
 *
 * @author Danil Kolikov
 */
@StorIOSQLiteType(table = Key.Contract.TABLE_NAME)
public class Key {
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_NETWORK, key = true)
    @Network
    int network;

    @SuppressWarnings("NullableProblems")   // Keys, stored in DB, have non-null value
    @NonNull
    @StorIOSQLiteColumn(name = Contract.COLUMN_NAME_VALUE)
    String value;

    public Key() {
    }

    public Key(int network, @NonNull String value) {
        this.network = network;
        this.value = value;
    }

    @NonNull
    public static Query selectAll() {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .build();
    }

    public static DeleteQuery deleteByNetwork(int network) {
        return DeleteQuery.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract.COLUMN_NAME_NETWORK + " = ?")
                .whereArgs(network)
                .build();
    }

    public int getNetwork() {
        return network;
    }

    public void setNetwork(int network) {
        this.network = network;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    public void setValue(@NonNull String value) {
        this.value = value;
    }

    @Nullable
    public KeyKeeper toKeyKeeper() {
        return KeyKeeper.fromStringBundle(getNetwork(), value);
    }

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
