package ly.loud.loudly.new_base;

import android.graphics.Point;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Networks.Network;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleImage;

/**
 * Image from some network
 *
 * @author Danil Kolikov
 */
public class Image implements SingleImage, SingleNetworkElement {
    @Nullable
    private String url;

    @NonNull
    private Point size;

    @Network
    private int network;

    @NonNull
    private Link link;

    @NonNull
    private Info info;

    public Image(@Nullable String url, @NonNull Point size, int network, @NonNull Link link) {
        this.url = url;
        this.size = size;
        this.network = network;
        this.link = link;
        info = new Info();
    }

    public Image(@Nullable String url, @NonNull Point size, int network, @NonNull Link link, @NonNull Info info) {
        this.url = url;
        this.size = size;
        this.network = network;
        this.link = link;
        this.info = info;
    }

    public Image(Parcel parcel) {
        url = parcel.readString();
        size = parcel.readParcelable(Point.class.getClassLoader());
        network = parcel.readInt();
        link = parcel.readParcelable(Link.class.getClassLoader());
        info = parcel.readParcelable(Info.class.getClassLoader());
    }

    @Nullable
    @Override
    public String getUrl() {
        return url;
    }

    @NonNull
    @Override
    public Point getSize() {
        return size;
    }

    @Override
    public int getNetwork() {
        return network;
    }

    @NonNull
    @Override
    public Link getLink() {
        return link;
    }

    @NonNull
    @Override
    public Info getInfo() {
        return info;
    }

    @Override
    public void setInfo(@NonNull Info newInfo) {
        this.info = newInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeParcelable(size, i);
        parcel.writeInt(network);
        parcel.writeParcelable(link, i);
        parcel.writeParcelable(info, i);
    }

    @Override
    public int getType() {
        return TYPE_IMAGE;
    }

    @Nullable
    @Override
    public String getExtra() {
        // Possible it won't be saved to database
        return url;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel parcel) {
            return new Image(parcel);
        }

        @Override
        public Image[] newArray(int i) {
            return new Image[i];
        }
    };
}
