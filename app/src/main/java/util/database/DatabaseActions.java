package util.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedList;

import base.Post;
import util.database.LoudlyPostsContract.*;

public class DatabaseActions {
    public static void savePosts(Context context, Post... posts) {
        SQLiteDatabase db = new LoudlyPostDbHelper(context).getWritableDatabase();
        for (Post post : posts) {
            ContentValues values = new ContentValues();
            values.put(PostEntry.COLUMN_NAME_TEXT, post.getText());
            values.put(PostEntry.COLUMN_NAME_ATTACHMENTS, "attachments here");
            values.put(PostEntry.COLUMN_NAME_INFOS, "infos");
            values.put(PostEntry.COLUMN_NAME_DATE, post.getDate());

            db.insert(PostEntry.TABLE_NAME, null, values);
        }
    }

    public static LinkedList<Post> loadPosts(Context context) {
        SQLiteDatabase db = new LoudlyPostDbHelper(context).getReadableDatabase();

        String[] projection = {
                PostEntry.COLUMN_NAME_TEXT,
                PostEntry.COLUMN_NAME_ATTACHMENTS,
                PostEntry.COLUMN_NAME_INFOS,
                PostEntry.COLUMN_NAME_DATE
        };

        String sortOrder = PostEntry.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(PostEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        LinkedList<Post> result = new LinkedList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String text = c.getString(c.getColumnIndex(PostEntry.COLUMN_NAME_TEXT));
            String attachments = c.getString(c.getColumnIndex(PostEntry.COLUMN_NAME_ATTACHMENTS));
            String infos = c.getString(c.getColumnIndex(PostEntry.COLUMN_NAME_INFOS));
            long date = c.getLong(c.getColumnIndex(PostEntry.COLUMN_NAME_DATE));
            Post post = new Post(text, date);
            Log.e("DB", attachments + " " + infos + " " + text);
            result.add(post);
        }
        return result;
    }
}
