package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.interfaces.says.SingleComment;

import java.util.ArrayList;

/**
 * Comment from some network
 *
 * @author Danil Kolikov
 */
public class Comment extends Say implements SingleComment {
    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @NonNull
    private final Person person;

    public Comment(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments,
                   @NonNull Person person, int network, @NonNull Link link) {
        super(text, date, attachments, network, link);
        this.person = person;
    }

    public Comment(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments,
                   @NonNull Person person, int network, @NonNull Link link, @NonNull Info info) {
        super(text, date, attachments, network, link, info);
        this.person = person;
    }

    protected Comment(Parcel parcel) {
        super(parcel);
        person = parcel.readParcelable(Person.class.getClassLoader());
    }

    @NonNull
    @Override
    public Person getPerson() {
        return person;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(person, flags);
    }
}
