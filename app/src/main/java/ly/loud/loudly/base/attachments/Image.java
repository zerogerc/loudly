package ly.loud.loudly.base.attachments;

import android.graphics.Point;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.new_base.Info;

/**
 * Image from the internet. Exists in only one network
 */
public class Image implements Attachment, SingleNetwork, Parcelable {
    protected String externalLink;
    protected Point size = new Point(0, 0);

    protected Info info;
    protected int network;
    protected Link id;

    public Image() {
        externalLink = null;
        size = new Point(0, 0);
        network = -1;
        id = null;
    }

    public Image(String externalLink, int network, Link id) {
        this.externalLink = externalLink;
        this.network = network;
        this.id = id;
    }

    public Image(String externalLink, Point size, int network, Link id) {
        this.externalLink = externalLink;
        this.size = size;
        this.network = network;
        this.id = id;
    }

    public Image(Parcel source) {
        externalLink = source.readString();
        size = source.readParcelable(Point.class.getClassLoader());
        info = source.readParcelable(Info.class.getClassLoader());
        network = source.readInt();
        id = source.readParcelable(Link.class.getClassLoader());
    }

    @Override
    public SingleNetwork getNetworkInstance(int network) {
        if (network == this.network) {
            return this;
        }
        return null;
    }

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

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public Uri getUri() {
        return Uri.parse(externalLink);
    }

    @Override
    public void setNetwork(int network) {
        this.network = network;
    }

    @Override
    public int getNetwork() {
        return network;
    }

    @Override
    public boolean exists() {
        return existsIn(network);
    }

    @Override
    public boolean existsIn(int network) {
        return this.network == network && id != null && id.isValid();
    }

    @Override
    public Link getLink() {
        return id;
    }

    @Override
    public void setLink(Link id) {
        this.id = id;
    }

    @Override
    public Info getInfo() {
        return info;
    }

    @Override
    public void setInfo(Info info) {
        this.info = info;
    }

    @Override
    public int getType() {
        return Attachment.IMAGE;
    }

    @Override
    public String getExtra() {
        return externalLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(externalLink);
        dest.writeParcelable(size, 0);
        dest.writeParcelable(info, 0);
        dest.writeInt(network);
        dest.writeParcelable(id, 0);
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}