package ly.loud.loudly.legacy_base.says;

import android.os.Parcel;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ly.loud.loudly.legacy_base.attachments.Attachment;
import ly.loud.loudly.base.entities.Link;
import ly.loud.loudly.base.entities.Location;

public class Post extends Say {
    @Nullable
    protected Location location;

    public Post() {
        super();
    }

    public Post(String text, int network, Link id) {
        super(text, network, id);
        location = null;
    }

    public Post(String text, long date, @Nullable Location location, int network, Link id) {
        super(text, date, network, id);
        this.location = location;

    }

    public Post(String text, ArrayList<Attachment> attachments,
                long date, @Nullable Location location, int network, Link id) {
        super(text, attachments, date, network, id);
        this.location = location;
    }

    public Post(Parcel source) {
        super(source);
        source.readParcelable(Location.class.getClassLoader());
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    public void setLocation(@Nullable Location location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(location, flags);
    }

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
}
