package ly.loud.loudly.base.single;

import android.os.Parcel;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Link;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainSay;
import ly.loud.loudly.ui.adapters.holders.ItemTypes;
import ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import ly.loud.loudly.ui.adapters.holders.ListItem;

/**
 * Comment from some network
 *
 * @author Danil Kolikov
 */
public class Comment extends PlainSay<SingleAttachment> implements SingleNetworkElement, ListItem {
    @NonNull
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

    public Comment(@Nullable String text,
                   long date,
                   @NonNull ArrayList<SingleAttachment> attachments,
                   @NonNull Person person,
                   int network,
                   @NonNull Link link) {
        super(text, date, attachments);
        this.person = person;
        this.network = network;
        this.link = link;
        this.info = new Info();
    }

    public Comment(@Nullable String text,
                   long date,
                   @NonNull ArrayList<SingleAttachment> attachments,
                   @NonNull Person person,
                   int network,
                   @NonNull Link link,
                   @NonNull Info info) {
        this(text, date, attachments, person, network, link);
        this.info = info;
    }

    protected Comment(@NonNull Parcel parcel) {
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

    @Override
    @NonNull
    public Link getLink() {
        return link;
    }

    @Override
    @NonNull
    public Info getInfo() {
        return info;
    }

    @NonNull
    @CheckResult
    @Override
    public Comment setInfo(@NonNull Info newInfo) {
        return new Comment(getText(), getDate(), getAttachments(), person, getNetwork(), link, newInfo);
    }

    @Override
    @ItemType
    public int getType() {
        return ItemTypes.COMMENT;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(network);
        dest.writeParcelable(link, flags);
        dest.writeParcelable(info, flags);
        dest.writeParcelable(person, flags);
    }
}
