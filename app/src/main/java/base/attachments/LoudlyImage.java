package base.attachments;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import base.Link;
import base.MultipleNetwork;
import base.Networks;
import base.says.Info;
import ly.loud.loudly.Loudly;
import util.Utils;

public class LoudlyImage extends Image implements MultipleNetwork, LocalFile {
    private Link[] ids;
    private Info[] infos;

    private Uri internalLink;

    public LoudlyImage() {
        super();
        ids = new Link[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
    }

    public LoudlyImage(String externalLink, Link[] ids) {
        internalLink = null;
        this.externalLink = externalLink;
        this.ids = ids;
        this.infos = new Info[Networks.NETWORK_COUNT];
    }

    public LoudlyImage(Uri internalLink) {
        this();
        this.internalLink = internalLink;
    }

    public void deleteInternalLink() {
        this.internalLink = null;
    }

    public boolean isLocal() {
        return externalLink == null;
    }

    @Override
    public boolean exists() {
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (existsIn(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsIn(int network) {
        return ids[network] != null && ids[network].isValid();
    }

    @Override
    public Link getId(int network) {
        return ids[network];
    }

    @Override
    public void setId(int network, Link link) {
        ids[network] = link;
        if (link == null) {
            setInfo(network, new Info());
        }
    }

    @Override
    public Link getId() {
        return getId(network);
    }

    @Override
    public void setId(Link id) {
        setId(network, id);
    }

    @Override
    public Link[] getIds() {
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

        //todo move summary info to the loudly info
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                this.info.add(infos[i]);
            }
        }
    }

    @Override
    public void setInfo(Info info) {
        setInfo(network, info);
    }

    @Override
    public Uri getUri() {
        if (internalLink != null) {
            return internalLink;
        } else {
            return Uri.parse(externalLink);
        }
    }

    @Override
    public String getMIMEType() {
        String type = Loudly.getContext().getContentResolver().getType(internalLink);
        if (type == null) {
            File file = new File(internalLink.toString());
            type = URLConnection.guessContentTypeFromName(file.getName());
        }
        return type;
    }

    @Override
    public InputStream getContent() throws IOException {
        InputStream stream  = Loudly.getContext().getContentResolver().openInputStream(internalLink);
        if (stream == null) {
            File file = new File(internalLink.toString());
            stream = new FileInputStream(file);
        }
        return stream;
    }

    @Override
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
