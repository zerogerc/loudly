package ly.loud.loudly.new_base;

import android.graphics.Point;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;

/**
 * Image from some network
 *
 * @author Danil Kolikov
 */
public class SingleImage extends PlainImage implements SingleAttachment {
    public static final Creator<SingleImage> CREATOR = new Creator<SingleImage>() {
        @Override
        public SingleImage createFromParcel(Parcel parcel) {
            return new SingleImage(parcel);
        }

        @Override
        public SingleImage[] newArray(int i) {
            return new SingleImage[i];
        }
    };

    @Network
    private final int network;

    @NonNull
    private final Link link;

    @NonNull
    private Info info;

    public SingleImage(@Nullable String url, @NonNull Point size, int network, @NonNull Link link) {
        super(url, size);
        this.network = network;
        this.link = link;
        info = new Info();
    }

    public SingleImage(@Nullable String url, @NonNull Point size, int network, @NonNull Link link, @NonNull Info info) {
        this(url, size, network, link);
        this.info = info;
    }

    private SingleImage(Parcel parcel) {
        super(parcel);
        network = parcel.readInt();
        link = parcel.readParcelable(Link.class.getClassLoader());
        info = parcel.readParcelable(Info.class.getClassLoader());
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
        super.writeToParcel(parcel, i);
        parcel.writeInt(network);
        parcel.writeParcelable(link, i);
        parcel.writeParcelable(info, i);
    }
}
