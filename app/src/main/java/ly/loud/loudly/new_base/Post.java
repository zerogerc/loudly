package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Location;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.interfaces.says.SinglePost;

import java.util.ArrayList;

/**
 * Post from some network
 *
 * @author Danil Kolikov
 */
public class Post extends Say implements SinglePost {
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Nullable
    private final Location location;

    public Post(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments, @Nullable Location location,
                int network, @NonNull Link link) {
        super(text, date, attachments, network, link);
        this.location = location;
    }

    public Post(@Nullable String text, long date, @NonNull ArrayList<SingleAttachment> attachments, @Nullable Location location,
                int network, @NonNull Link link, @NonNull Info info) {
        super(text, date, attachments, network, link, info);
        this.location = location;
    }

    protected Post(Parcel parcel) {
        super(parcel);
        location = parcel.readParcelable(Location.class.getClassLoader());
    }

    @Nullable
    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(location, flags);
    }
}
