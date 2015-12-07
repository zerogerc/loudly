package base.attachments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.IOException;
import java.io.InputStream;

import base.Networks;
import base.Person;
import ly.loud.loudly.Loudly;
import util.Utils;

public class Image extends Attachment {
    protected String[] links;
    protected String internalLink;
    protected Point size = new Point(0, 0);
    protected boolean local;

    public void setWidth(int width) {
        size.x = width;
    }

    public void setHeight(int height) {
        size.y = height;
    }

    public int getWidth() {
        return size.x;
    }

    public int getHeight() {
        return size.y;
    }

    public Point getSize() {
        return size;
    }

    public void setSize(Point size) {
        this.size = size;
    }

    {

        localID = 0;
    }

    public Image(String internalLink, String[] links) {
        this.internalLink = internalLink;
        this.links = links;
        this.local = true;
    }

    public Image(String internalLink, boolean local) {
        this.internalLink = internalLink;
        this.local = local;
        this.links = new String[Networks.NETWORK_COUNT];
    }

    public Image(Uri internalLink) {
        this.internalLink = internalLink.toString();
        this.links = new String[Networks.NETWORK_COUNT];
        this.local = true;
    }

    public Uri getUri() {
        return Uri.parse(internalLink);
    }

    public String getMIMEType() {
        return Loudly.getContext().getContentResolver().getType(getUri());
    }

    public InputStream getContent() throws IOException {
        return Loudly.getContext().getContentResolver().openInputStream(getUri());
    }

    public long getFileSize() throws IOException {
        Cursor cursor = null;
        try {
            cursor = Loudly.getContext().getContentResolver().query(getUri(), null, null, null, null);
            if (cursor == null) {
                return 0;
            }
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        } finally {
            Utils.closeQuietly(cursor);
        }

    }

    public void setLink(int network, String link) {
        links[network] = link;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public int getType() {
        return Attachment.IMAGE;
    }

    @Override
    public String[] getLinks() {
        return links;
    }

    @Override
    public String getLink(int network) {
        return links[network];
    }

    @Override
    public String getExtra() {
        return internalLink;
    }
}