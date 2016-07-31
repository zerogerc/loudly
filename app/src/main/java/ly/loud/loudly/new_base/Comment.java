package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainSay;
import ly.loud.loudly.ui.adapter.Item;

import java.util.ArrayList;

/**
 * Comment from some network
 *
 * @author Danil Kolikov
 */
public class Comment extends PlainSay<SingleAttachment> implements SingleNetworkElement, Item {
    public final static Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel parcel) {
            return new Comment(parcel);
        }

        @Override
        public Comment[] newArray(int i) {
            return new Comment[i];
        }
    };

    @NonNull
    private final Person person;

    private final int network;

    @NonNull
    private final Link link;

    @NonNull
    private Info info;

    public Comment(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments,
                   @NonNull Person person, int network, @NonNull Link link) {
        super(text, date, attachments);
        this.person = person;
        this.network = network;
        this.link = link;
        this.info = new Info();
    }

    public Comment(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments,
                   @NonNull Person person, int network, @NonNull Link link, @NonNull Info info) {
        this(text, date, attachments, person, network, link);
        this.info = info;
    }

    protected Comment(Parcel parcel) {
        super(parcel);
        network = parcel.readInt();
        link = Link.CREATOR.createFromParcel(parcel);
        info = Info.CREATOR.createFromParcel(parcel);
        person = parcel.readParcelable(Person.class.getClassLoader());
    }

    @NonNull
    public Person getPerson() {
        return person;
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
    public int getType() {
        return COMMENT;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(network);
        dest.writeParcelable(link, flags);
        dest.writeParcelable(info, flags);
        dest.writeParcelable(person, flags);
    }
}
