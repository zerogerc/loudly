package ly.loud.loudly.util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import ly.loud.loudly.util.database.entities.StoredEvent;
import ly.loud.loudly.util.database.entities.StoredAttachment;
import ly.loud.loudly.util.database.entities.StoredLocation;
import ly.loud.loudly.util.database.entities.StoredPost;
import ly.loud.loudly.util.database.entities.links.Links;

/**
 * Helper for Post Database
 */
public class PostDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "posts.db";

    PostDbHelper(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StoredLocation.Contract.SQL_CREATE_TABLE);
        db.execSQL(Links.Contract.SQL_CREATE_TABLE);
        db.execSQL(StoredPost.Contract.SQL_CREATE_POST_TABLE);
        db.execSQL(StoredAttachment.Contract.SQL_CREATE_TABLE);
        db.execSQL(StoredEvent.Contract.SQL_CREATE_TABLE);
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
