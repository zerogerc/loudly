package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainPost;

import java.util.ArrayList;

/**
 * Post that exists in one network
 *
 * @author Danil Kolikov
 */
public class SinglePost extends PlainPost<SingleAttachment> implements SingleNetworkElement {
    @NonNull
    public static final Creator<SinglePost> CREATOR = new Creator<SinglePost>() {
        @Override
        public SinglePost createFromParcel(Parcel parcel) {
            return new SinglePost(parcel);
        }

        @Override
        public SinglePost[] newArray(int i) {
            return new SinglePost[i];
        }
    };

    @Network
    private final int network;

    @NonNull
    private final Link link;

    @NonNull
    private Info info;

    public SinglePost(@Nullable String text,
                      long date,
                      @NonNull ArrayList<SingleAttachment> attachments,
                      @Nullable Location location,
                      int network,
                      @NonNull Link link,
                      @NonNull Info info) {
        super(text, date, attachments, location);
        this.network = network;
        this.link = link;
        this.info = info;
    }

    private SinglePost(@Nullable String text,
                       long date,
                       @NonNull ArrayList<SingleAttachment> attachments,
                      @Nullable Location location,
                       int network,
                       @NonNull Link link) {
        super(text, date, attachments, location);
        this.network = network;
        this.link = link;
        info = new Info();
    }

    public SinglePost(@NonNull PlainPost<SingleAttachment> plainPost,
                      @Network int network,
                      @NonNull Link link) {
        this(plainPost.getText(), plainPost.getDate(), plainPost.getAttachments(), plainPost.getLocation(),
                network, link);
    }

    private SinglePost(@NonNull Parcel parcel) {
        super(parcel);
        network = parcel.readInt();
        link = Link.CREATOR.createFromParcel(parcel);
        info = Info.CREATOR.createFromParcel(parcel);
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
        info = newInfo;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(network);
        parcel.writeParcelable(link, i);
        parcel.writeParcelable(info, i);
    }
}
