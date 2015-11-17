package util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ly.loud.loudly.Loudly;
import util.database.KeysContract.KeysEntry;

public class KeysDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "keys.db";

    private final Context context;
    private static volatile KeysDbHelper self;

    public static KeysDbHelper getInstance() {
        if (self == null) {
            synchronized (KeysDbHelper.class) {
                if (self == null) {
                    self = new KeysDbHelper(Loudly.getContext());
                }
            }
        }
        return self;
    }

    private KeysDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private static final String SQL_CREATE_ENTRY =
            "CREATE TABLE " + KeysEntry.TABLE_NAME + " ("
            + KeysEntry._ID+ " INTEGER PRIMARY KEY, "
            + KeysEntry.COLUMN_NAME_NETWORK + " INTEGER, "
            + KeysEntry.COLUMN_NAME_VALUE + " TEXT )";

    private static final String SQL_DELETE_ENTRY =
            "DROP TABLE IF EXISTS " + KeysEntry.TABLE_NAME;


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRY);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
