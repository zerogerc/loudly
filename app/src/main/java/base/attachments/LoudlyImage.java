package base.attachments;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import base.MultipleNetwork;
import base.Networks;
import base.says.Info;
import ly.loud.loudly.Loudly;
import util.Utils;

public class LoudlyImage extends Image implements MultipleNetwork {
    private String[] ids;
    private Info[] infos;
    private long localId = 0;

    private Uri internalLink;

    public LoudlyImage() {
        super();
        ids = new String[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
    }

    public LoudlyImage(String externalLink, String[] ids) {
        internalLink = null;
        this.externalLink = externalLink;
        this.ids = ids;
        this.infos = new Info[Networks.NETWORK_COUNT];
    }

    public LoudlyImage(Uri internalLink) {
        this();
        this.internalLink = internalLink;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public void deleteInternalLink() {
        this.internalLink = null;
    }

    public boolean isLocal() {
        return externalLink == null;
    }

    @Override
    public String getId(int network) {
        return ids[network];
    }

    @Override
    public void setId(int network, String link) {
        ids[network] = link;
    }

    @Override
    public String getId() {
        return ids[network];
    }

    @Override
    public void setId(String id) {
        ids[network] = id;
    }

    @Override
    public String[] getIds() {
        return ids;
    }

    @Override
    public Info getInfo(int network) {
        return infos[network];
    }

    @Override
    public void setInfo(int network, Info info) {
        infos[network] = info;
        this.info = new Info();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (existsIn(i)) {
                this.info.add(infos[i]);
            }
        }
    }

    @Override
    public String getExtra() {
        return externalLink;
    }

    public Uri getUri() {
        if (internalLink != null) {
            return internalLink;
        } else {
            return Uri.parse(externalLink);
        }
    }

    public String getMIMEType() {
        String type = Loudly.getContext().getContentResolver().getType(internalLink);
        if (type == null) {
            File file = new File(internalLink.toString());
            type = URLConnection.guessContentTypeFromName(file.getName());
        }
        return type;
    }

    public InputStream getContent() throws IOException {
        InputStream stream  = Loudly.getContext().getContentResolver().openInputStream(internalLink);
        if (stream == null) {
            File file = new File(internalLink.toString());
            stream = new FileInputStream(file);
        }
        return stream;
    }

    public long getFileSize() throws IOException {
        Cursor cursor = null;
        try {
            cursor = Loudly.getContext().getContentResolver().query(internalLink, null, null, null, null);
            if (cursor == null) {
                return new File(internalLink.toString()).length();
            }
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        } finally {
            Utils.closeQuietly(cursor);
        }
    }
}
