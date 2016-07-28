package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Networks.Network;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.interfaces.says.SingleSay;

import java.util.ArrayList;

/**
 * Just say from network (post, comment, ...)
 *
 * @author Danil Kolikov
 */
public class Say implements SingleSay {
    public static final Creator<Say> CREATOR = new Creator<Say>() {
        @Override
        public Say createFromParcel(Parcel source) {
            return new Say(source);
        }

        @Override
        public Say[] newArray(int size) {
            return new Say[size];
        }
    };

    @Nullable
    private final String text;

    private final long date;

    @NonNull
    private final ArrayList<SingleAttachment> attachments;

    @Network
    private final int network;

    @NonNull
    private final Link link;

    @NonNull
    private Info info;

    public Say(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments,
               int network, @NonNull Link link) {
        this.text = text;
        this.date = date;
        this.attachments = attachments;
        this.network = network;
        this.link = link;
        info = new Info();
    }

    public Say(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments,
               int network, @NonNull Link link, @NonNull Info info) {
        this.text = text;
        this.date = date;
        this.attachments = attachments;
        this.network = network;
        this.link = link;
        this.info = info;
    }

    protected Say(Parcel parcel) {
        text = parcel.readString();
        //TODO: check this
        attachments = parcel.readArrayList(getClass().getClassLoader());
        date = parcel.readLong();
        network = parcel.readInt();
        link = parcel.readParcelable(Link.class.getClassLoader());
        info = parcel.readParcelable(Info.class.getClassLoader());
    }

    @Nullable
    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getDate() {
        return date;
    }

    @NonNull
    @Override
    public ArrayList<SingleAttachment> getAttachments() {
        return attachments;
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
        parcel.writeString(text);
        parcel.writeList(attachments);
        parcel.writeLong(date);
        parcel.writeInt(network);
        parcel.writeParcelable(link, i);
        parcel.writeParcelable(info, i);
    }
}
