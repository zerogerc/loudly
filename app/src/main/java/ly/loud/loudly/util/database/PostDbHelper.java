package ly.loud.loudly.util.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.util.database.entities.Attachment;
import ly.loud.loudly.util.database.entities.links.Links;
import ly.loud.loudly.util.database.entities.Location;
import ly.loud.loudly.util.database.entities.Post;

/**
 * Helper for Post Database
 */
public class PostDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "posts.db";

    PostDbHelper() {
        super(Loudly.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Location.Contract.SQL_CREATE_TABLE);
        db.execSQL(Links.Contract.SQL_CREATE_TABLE);
        db.execSQL(Post.Contract.SQL_CREATE_POST_TABLE);
        db.execSQL(Attachment.Contract.SQL_CREATE_TABLE);
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
