package util.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedList;

import base.Location;
import base.Networks;
import base.Post;
import base.attachments.Attachment;
import util.database.PostContract.PostEntry;
import util.database.LinksContract.LinksEntry;
import util.database.AttachmentsContract.AttachmentsEntry;
import util.database.LocationContract.LocationEntry;

public class DatabaseActions {
    private static ContentValues createLinkRow(String[] links) {
        ContentValues result = new ContentValues();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (links[i] != null) {
                result.put(PostDbHelper.NETWORKS_COLUMNS[i], links[i]);
            }
        }
        return result;
    }

    public static void savePost(Post post) {
        SQLiteDatabase db = PostDbHelper.getInstance().getWritableDatabase();

        // ToDo: catch -1 in ids
        db.beginTransaction();
        try {

            // Insert post's links
            ContentValues links = createLinkRow(post.getLinks());
            long linksId = db.insert(LinksEntry.TABLE_NAME, null, links);

            // Insert location
            Location location = post.getLocation();
            ContentValues loc = new ContentValues();
            loc.put(LocationEntry.COLUMN_NAME_LATITUDE, location.latitude);
            loc.put(LocationEntry.COLUMN_NAME_LONGITUDE, location.longitude);
            loc.put(LocationEntry.COLUMN_NAME_NAME, location.name);
            long locID = db.insert(LocationEntry.TABLE_NAME, null, loc);

            long prevId = -1;
            long firstId = -1;
            for (Attachment attachment : post.getAttachments()) {
                // Insert attachment's links
                ContentValues atLink = createLinkRow(attachment.getLinks());
                long atLinkId = db.insert(LinksEntry.TABLE_NAME, null, atLink);

                // Insert attachment
                ContentValues atVal = new ContentValues();
                atVal.put(AttachmentsEntry.COLUMN_NAME_TYPE, attachment.getType());
                atVal.put(AttachmentsEntry.COLUMN_NAME_PREV, prevId);
                atVal.put(AttachmentsEntry.COLUMN_NAME_NEXT, -1);
                atVal.put(AttachmentsEntry.COLUMN_NAME_LINK, atLinkId);

                long curId = db.insert(AttachmentsEntry.TABLE_NAME, null, atVal);

                // Update link to the next in previous
                ContentValues update = new ContentValues();
                update.put(AttachmentsEntry.COLUMN_NAME_NEXT, curId);
                db.update(AttachmentsEntry.TABLE_NAME, update, "_id=" + prevId, null);

                if (prevId == -1) {
                    firstId = curId;
                }
                prevId = curId;
            }

            // Insert post to the DB
            ContentValues values = new ContentValues();
            values.put(PostEntry.COLUMN_NAME_TEXT, post.getText());
            values.put(PostEntry.COLUMN_NAME_FIRST_ATTACHMENT, firstId);
            values.put(PostEntry.COLUMN_NAME_LINKS, linksId);
            values.put(PostEntry.COLUMN_NAME_DATE, post.getDate());
            values.put(PostEntry.COLUMN_NAME_LOCATION, locID);
            db.insert(PostEntry.TABLE_NAME, null, values);

            // Success
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void saveKeys() {
        SQLiteDatabase db = KeysDbHelper.getInstance().getWritableDatabase();

    }
    public static LinkedList<Post> loadPosts(Context context) {
        SQLiteDatabase db = PostDbHelper.getInstance().getReadableDatabase();

        String[] projection = {
                PostEntry.COLUMN_NAME_TEXT,
                PostEntry.COLUMN_NAME_FIRST_ATTACHMENT,
                PostEntry.COLUMN_NAME_LINKS,
                PostEntry.COLUMN_NAME_DATE
        };

        String sortOrder = PostEntry.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(PostEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        LinkedList<Post> result = new LinkedList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String text = c.getString(c.getColumnIndex(PostEntry.COLUMN_NAME_TEXT));
            String attachments = c.getString(c.getColumnIndex(PostEntry.COLUMN_NAME_FIRST_ATTACHMENT));
            String infos = c.getString(c.getColumnIndex(PostEntry.COLUMN_NAME_LINKS));
            long date = c.getLong(c.getColumnIndex(PostEntry.COLUMN_NAME_DATE));
            Post post = new Post(text);
            Log.e("DB", attachments + " " + infos + " " + text);
            result.add(post);
        }
        return result;
    }
}
