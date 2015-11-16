package util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import util.database.LoudlyPostsContract.PostEntry;

public class LoudlyPostDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "posts.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA = ",";
    private static final String SQL_CREATE_ENTRY =
            "CREATE TABLE " + PostEntry.TABLE_NAME + " ("
                    + PostEntry._ID + " INTEGER PRIMARY KEY,"
                    + PostEntry.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA
                    + PostEntry.COLUMN_NAME_ATTACHMENTS + TEXT_TYPE + COMMA
                    + PostEntry.COLUMN_NAME_INFOS + TEXT_TYPE + COMMA
                    + PostEntry.COLUMN_NAME_DATE + " LONG )";

    private static final String SQL_DELETE_ENTRY =
            "DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME;

    public LoudlyPostDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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
