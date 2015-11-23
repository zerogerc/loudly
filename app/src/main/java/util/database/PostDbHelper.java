package util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ly.loud.loudly.Loudly;
import util.database.AttachmentsContract.AttachmentsEntry;
import util.database.LinksContract.LinksEntry;
import util.database.LocationContract.LocationEntry;
import util.database.PostContract.PostEntry;

public class PostDbHelper extends SQLiteOpenHelper {
    private static volatile PostDbHelper self;


    public static PostDbHelper getInstance() {
        if (self == null) {
            synchronized (PostDbHelper.class) {
                if (self == null) {
                    self = new PostDbHelper();
                }
            }
        }
        return self;
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "posts.db";

    private static final String SQL_CREATE_POST_TABLE =
            "CREATE TABLE " + PostEntry.TABLE_NAME + " ("
                    + PostEntry._ID + " INTEGER PRIMARY KEY, "
                    + PostEntry.COLUMN_NAME_TEXT + " TEXT, "
                    + PostEntry.COLUMN_NAME_FIRST_ATTACHMENT + " INTEGER, "
                    + PostEntry.COLUMN_NAME_LINKS + " INTEGER, "
                    + PostEntry.COLUMN_NAME_DATE + " LONG, "
                    + PostEntry.COLUMN_NAME_LOCATION + " INTEGER )";

    private static final String SQL_CREATE_ATTACHMENTS_TABLE =
            "CREATE TABLE " + AttachmentsEntry.TABLE_NAME + " ("
                    + AttachmentsEntry._ID + " INTEGER PRIMARY KEY, "
                    + AttachmentsEntry.COLUMN_NAME_TYPE + " INTEGER, "
                    + AttachmentsEntry.COLUMN_NAME_LINK + " INTEGER, "
                    + AttachmentsEntry.COLUMN_NAME_EXTRA + " TEXT, "
                    + AttachmentsEntry.COLUMN_NAME_NEXT + " INTEGER, "
                    + AttachmentsEntry.COLUMN_NAME_PREV + " INTEGER )";

    private static final String SQL_CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LocationEntry.TABLE_NAME + " ("
                    + LocationEntry._ID + " INTEGER PRIMARY KEY, "
                    + LocationEntry.COLUMN_NAME_LATITUDE + " REAL, "
                    + LocationEntry.COLUMN_NAME_LONGITUDE + " REAL, "
                    + LocationEntry.COLUMN_NAME_NAME + " TEXT )";

    private static final String SQL_CREATE_LINKS_TABLE =
            "CREATE TABLE " + LinksEntry.TABLE_NAME + " ("
                    + LinksEntry._ID + " INTEGER PRIMARY KEY, "
                    + LinksEntry.COLUMN_NAME_FB + " TEXT, "
                    + LinksEntry.COLUMN_NAME_TWITTER + " TEXT, "
                    + LinksEntry.COLUMN_NAME_INSTAGRAM + " TEXT, "
                    + LinksEntry.COLUMN_NAME_VK + " TEXT, "
                    + LinksEntry.COLUMN_NAME_OK + " TEXT, "
                    + LinksEntry.COLUMN_NAME_MAILRU + " TEXT )";

    private static String sqlDeleteTable(String table) {
        return "DROP TABLE IF EXISTS " + table;
    }

    private PostDbHelper() {
        super(Loudly.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_POST_TABLE);
        db.execSQL(SQL_CREATE_ATTACHMENTS_TABLE);
        db.execSQL(SQL_CREATE_LINKS_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade here
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
