package util.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.provider.SyncStateContract;

import java.util.ArrayList;

import base.KeyKeeper;
import base.Location;
import base.Networks;
import base.Post;
import base.attachments.Attachment;
import ly.loud.loudly.Loudly;
import util.Network;
import util.database.AttachmentsContract.AttachmentsEntry;
import util.database.KeysContract.KeysEntry;
import util.database.LinksContract.LinksEntry;
import util.database.LocationContract.LocationEntry;
import util.database.PostContract.PostEntry;

public class DatabaseActions {

    public static void savePost(Post post) throws DatabaseException {
        SQLiteDatabase db = PostDbHelper.getInstance().getWritableDatabase();

        // ToDo: catch -1 in ids
        db.beginTransaction();
        try {

            // Insert post's links
            long linksId = db.insert(LinksEntry.TABLE_NAME, null,
                    createLinkRow(post.getLinks()));

            // Insert location
            long locID = db.insert(LocationEntry.TABLE_NAME, null,
                    createLocationRow(post.getLocation()));

            long prevId = AttachmentsEntry.END_OF_LIST_VALUE;
            long firstId = AttachmentsEntry.END_OF_LIST_VALUE;
            for (Attachment attachment : post.getAttachments()) {
                // Insert attachment's links
                long atLinkId = db.insert(LinksEntry.TABLE_NAME, null,
                        createLinkRow(attachment.getLinks()));

                // Insert attachment
                long curId = db.insert(AttachmentsEntry.TABLE_NAME, null,
                        createAttachmentsRow(attachment, prevId, atLinkId));

                // Update link to the next in previous
                ContentValues update = new ContentValues();
                update.put(AttachmentsEntry.COLUMN_NAME_NEXT, curId);
                db.update(AttachmentsEntry.TABLE_NAME, update, sqlEqual(AttachmentsEntry._ID, prevId), null);

                if (prevId == AttachmentsEntry.END_OF_LIST_VALUE) {
                    firstId = curId;
                }
                prevId = curId;
            }

            // Insert post to the DB

            long localId = db.insert(PostEntry.TABLE_NAME, null,
                    createPostRow(post, firstId, linksId, locID));

            post.setLocalId(localId);

            // Success
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void loadPosts() throws DatabaseException {
        SQLiteDatabase db = PostDbHelper.getInstance().getReadableDatabase();
        Loudly context = Loudly.getContext();

        String sortOrder = PostEntry.COLUMN_NAME_DATE + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.query(
                    PostEntry.TABLE_NAME,
                    PostEntry.POST_COLUMNS,
                    null, null, null, null, sortOrder);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String text = cursor.getString(cursor.getColumnIndex(PostEntry.COLUMN_NAME_TEXT));
                long date = cursor.getLong(cursor.getColumnIndex(PostEntry.COLUMN_NAME_DATE));

                long atId = cursor.getLong(cursor.getColumnIndex(PostEntry.COLUMN_NAME_FIRST_ATTACHMENT));
                long linksId = cursor.getLong(cursor.getColumnIndex(PostEntry.COLUMN_NAME_LINKS));
                long locId = cursor.getLong(cursor.getColumnIndex(PostEntry.COLUMN_NAME_LOCATION));
                long localId = cursor.getLong(cursor.getColumnIndex(PostEntry._ID));

                Location location = readLocation(db, locId);
                String[] links = readLinks(db, linksId);
                ArrayList<Attachment> attachments = readAttachments(db, atId);

                Post post = new Post(text, attachments, links, date, location, localId);
                context.addPost(post);

                cursor.moveToNext();
            }
        } finally {
            Network.closeQuietly(cursor);
        }
    }

    public static void saveKeys() throws DatabaseException {
        SQLiteDatabase db = KeysDbHelper.getInstance().getWritableDatabase();
        Loudly context = Loudly.getContext();

        // ToDo: check -1
        db.beginTransaction();
        try {
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                if (context.getKeyKeeper(i) != null) {
                    KeyKeeper keys = context.getKeyKeeper(i);

                    ContentValues values = new ContentValues();
                    values.put(KeysEntry.COLUMN_NAME_NETWORK, i);
                    values.put(KeysEntry.COLUMN_NAME_VALUE, keys.toStringBundle());

                    long keyId = upsert(db, KeysEntry.TABLE_NAME,
                            KeysEntry.COLUMN_NAME_NETWORK, Integer.toString(i), values);

                    if (keyId == -1) {
                        throw new DatabaseException("Can't save key for the network # " + i);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void loadKeys() throws DatabaseException {
        SQLiteDatabase db = KeysDbHelper.getInstance().getReadableDatabase();
        Loudly context = Loudly.getContext();

        Cursor cursor = null;
        try {
            String[] projection = {
                    KeysEntry.COLUMN_NAME_NETWORK,
                    KeysEntry.COLUMN_NAME_VALUE
            };
            cursor = db.query(KeysEntry.TABLE_NAME, projection, null, null, null, null, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                long c = cursor.getCount();
                int network = cursor.getInt(cursor.getColumnIndex(KeysEntry.COLUMN_NAME_NETWORK));
                String bundle = cursor.getString(cursor.getColumnIndex(KeysEntry.COLUMN_NAME_VALUE));

                context.setKeyKeeper(network, KeyKeeper.fromStringBundle(network, bundle));
                cursor.moveToNext();
            }
        } finally {
            Network.closeQuietly(cursor);
        }
    }

    private static long upsert(SQLiteDatabase db, String tableName,
                               String findByColumn, String columnValue, ContentValues values) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    tableName,
                    new String[]{findByColumn},
                    sqlEqual(findByColumn, columnValue),
                    null, null, null, null);

            if (cursor.getCount() == 0) {
                return db.insert(tableName, null, values);
            } else {
                int count = db.update(tableName,
                        values,
                        sqlEqual(findByColumn, columnValue),
                        null);

                return (count == 1) ? 0 : -1;
            }
        } finally {
            Network.closeQuietly(cursor);
        }
    }

    private static ContentValues createLinkRow(String[] links) {
        ContentValues result = new ContentValues();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (links[i] != null) {
                result.put(LinksEntry.LINK_COLUMNS[i], links[i]);
            }
        }
        return result;
    }

    private static ContentValues createLocationRow(Location location) {
        ContentValues result = new ContentValues();
        result.put(LocationEntry.COLUMN_NAME_LATITUDE, location.latitude);
        result.put(LocationEntry.COLUMN_NAME_LONGITUDE, location.longitude);
        result.put(LocationEntry.COLUMN_NAME_NAME, location.name);
        return result;
    }

    private static ContentValues createAttachmentsRow(Attachment attachment, long prev, long atLink) {
        ContentValues result = new ContentValues();
        result.put(AttachmentsEntry.COLUMN_NAME_TYPE, attachment.getType());
        result.put(AttachmentsEntry.COLUMN_NAME_EXTRA, attachment.getExtra());
        result.put(AttachmentsEntry.COLUMN_NAME_PREV, prev);
        result.put(AttachmentsEntry.COLUMN_NAME_NEXT, AttachmentsEntry.END_OF_LIST_VALUE);
        result.put(AttachmentsEntry.COLUMN_NAME_LINK, atLink);
        return result;
    }

    private static ContentValues createPostRow(Post post, long atId, long linkId, long locId) {
        ContentValues values = new ContentValues();
        values.put(PostEntry.COLUMN_NAME_TEXT, post.getText());
        values.put(PostEntry.COLUMN_NAME_FIRST_ATTACHMENT, atId);
        values.put(PostEntry.COLUMN_NAME_LINKS, linkId);
        values.put(PostEntry.COLUMN_NAME_DATE, post.getDate());
        values.put(PostEntry.COLUMN_NAME_LOCATION, locId);
        return values;
    }

    private static Location readLocation(SQLiteDatabase db, long locId) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    LocationEntry.TABLE_NAME,
                    LocationEntry.LOCATION_COLUMNS,
                    sqlEqual(LocationEntry._ID, locId),
                    null, null, null, null);

            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(LocationEntry.COLUMN_NAME_NAME));
            double longitude = cursor.getDouble(cursor.getColumnIndex(LocationEntry.COLUMN_NAME_LONGITUDE));
            double latitude = cursor.getDouble(cursor.getColumnIndex(LocationEntry.COLUMN_NAME_LATITUDE));

            return new Location(latitude, longitude, name);
        } finally {
            Network.closeQuietly(cursor);
        }
    }

    private static String[] readLinks(SQLiteDatabase db, long linksId) {
        Cursor cursor = null;
        try {
            cursor = db.query(
                    LinksEntry.TABLE_NAME,
                    LinksEntry.LINK_COLUMNS,
                    sqlEqual(LocationEntry._ID, linksId),
                    null, null, null, null);

            cursor.moveToFirst();

            String[] links = new String[Networks.NETWORK_COUNT];
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                links[i] = cursor.getString(cursor.getColumnIndex(LinksEntry.LINK_COLUMNS[i]));
            }
            return links;
        } finally {
            Network.closeQuietly(cursor);
        }
    }

    private static ArrayList<Attachment> readAttachments(SQLiteDatabase db, long firstId) {
        ArrayList<Attachment> list = new ArrayList<>();

        Cursor cursor = null;
        long nextId = firstId;

        try {
            while (nextId != AttachmentsEntry.END_OF_LIST_VALUE) {
                cursor = db.query(
                        AttachmentsEntry.TABLE_NAME,
                        AttachmentsEntry.ATTACHMENTS_COLUMNS,
                        sqlEqual(AttachmentsEntry._ID, nextId),
                        null, null, null, null);

                cursor.moveToFirst();

                int type = cursor.getInt(cursor.getColumnIndex(AttachmentsEntry.COLUMN_NAME_TYPE));
                String extra = cursor.getString(cursor.getColumnIndex(AttachmentsEntry.COLUMN_NAME_EXTRA));
                nextId = cursor.getLong(cursor.getColumnIndex(AttachmentsEntry.COLUMN_NAME_NEXT));
                long linkId = cursor.getLong(cursor.getColumnIndex(AttachmentsEntry.COLUMN_NAME_LINK));

                String[] links = readLinks(db, linkId);
                list.add(Attachment.makeAttachment(type, extra, links));
            }
        } finally {
            Network.closeQuietly(cursor);
        }
        return list;
    }

    private static String sqlEqual(String column, long value) {
        return sqlEqual(column, Long.toString(value));
    }
    private static String sqlEqual(String column, String value) {
        return column + " = " + value;
    }
}
