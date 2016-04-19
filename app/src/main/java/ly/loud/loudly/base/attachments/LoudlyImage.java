package ly.loud.loudly.base.attachments;

import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.MultipleNetwork;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.ui.Loudly;
import ly.loud.loudly.util.Utils;

public class LoudlyImage extends Image implements MultipleNetwork, LocalFile {
    private Link[] ids;
    private Info[] infos;

    private Uri internalLink;

    private static class LoudlyImageProxy extends Image implements LocalFile {
        LoudlyImage parent;

        public LoudlyImageProxy(LoudlyImage parent, int network) {
            this.parent = parent;
            this.network = network;
        }

        @Override
        public void setWidth(int width) {
            parent.setWidth(width);
        }

        @Override
        public void setHeight(int height) {
            parent.setHeight(height);
        }

        @Override
        public int getWidth() {
            return parent.getWidth();
        }

        @Override
        public int getHeight() {
            return parent.getHeight();
        }

        @Override
        public Point getSize() {
            return parent.getSize();
        }

        @Override
        public void setSize(Point size) {
            parent.setSize(size);
        }

        @Override
        public String getExternalLink() {
            return parent.getExternalLink();
        }

        @Override
        public void setExternalLink(String externalLink) {
            parent.setExternalLink(externalLink);
        }

        @Override
        public Uri getUri() {
            return parent.getUri();
        }

        @Override
        public int getNetwork() {
            return super.getNetwork();
        }

        @Override
        public boolean exists() {
            return existsIn(network);
        }

        @Override
        public boolean existsIn(int network) {
            return parent.existsIn(network);
        }

        @Override
        public Link getLink() {
            return parent.getLink(network);
        }

        @Override
        public void setLink(Link id) {
            parent.setLink(network, id);
        }

        @Override
        public Info getInfo() {
            return parent.getInfo(network);
        }

        @Override
        public void setInfo(Info info) {
            parent.setInfo(network, info);
        }

        @Override
        public int getType() {
            return parent.getType();
        }

        @Override
        public String getExtra() {
            return parent.getExtra();
        }

        @Override
        public String getMIMEType() {
            return parent.getMIMEType();
        }

        @Override
        public InputStream getContent() throws IOException {
            return parent.getContent();
        }

        @Override
        public long getFileSize() throws IOException {
            return parent.getFileSize();
        }
    }

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

    public LoudlyImage(Parcel source) {
        externalLink = source.readString();
        size = source.readParcelable(Point.class.getClassLoader());
        info = source.readParcelable(Info.class.getClassLoader());
        network = source.readInt();
        id = source.readParcelable(Link.class.getClassLoader());
    }

    public void deleteInternalLink() {
        this.internalLink = null;
    }

    public boolean isLocal() {
        return externalLink == null;
    }

    @Override
    public SingleNetwork getNetworkInstance(int network) {
        if (network == Networks.LOUDLY) {
            return this;
        }
        return new LoudlyImageProxy(this, network);
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
    public Link getLink(int network) {
        return ids[network];
    }

    @Override
    public void setLink(int network, Link link) {
        ids[network] = link;
        if (link == null) {
            setInfo(network, new Info());
        }
    }

    @Override
    public Link[] getLinks() {
        return ids;
    }

    @Override
    public Info getInfo(int network) {
        return infos[network];
    }

    @Override
    public void setInfo(int network, Info info) {
        infos[network] = info;
        infos[Networks.LOUDLY] = new Info();

        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                infos[Networks.LOUDLY].add(infos[i]);
            }
        }
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
        InputStream stream = Loudly.getContext().getContentResolver().openInputStream(internalLink);
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

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(externalLink);
//        dest.writeParcelable(size, 0);
//        dest.writeParcelable(info, 0);
//        dest.writeInt(network);
//        dest.writeParcelable(id, 0);
//    }
//
//    public static final Creator<Image> CREATOR = new Creator<Image>() {
//        @Override
//        public Image createFromParcel(Parcel source) {
//            return new Image(source);
//        }
//
//        @Override
//        public Image[] newArray(int size) {
//            return new Image[size];
//        }
//    };
}
