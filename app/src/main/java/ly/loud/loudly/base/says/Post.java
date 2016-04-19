package ly.loud.loudly.base.says;

import android.os.Parcel;

import java.util.ArrayList;

import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Location;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.ui.adapter.Item;

public class Post extends Say implements Item {
    protected Location location;

    public Post() {
        super();
    }

    public Post(String text, int network, Link id) {
        super(text, network, id);
        location = null;
    }

    public Post(String text, long date, Location location, int network, Link id) {
        super(text, date, network, id);
        this.location = location;

    }

    public Post(String text, ArrayList<Attachment> attachments,
                long date, Location location, int network, Link id) {
        super(text, attachments, date, network, id);
        this.location = location;
    }

    public Post(Parcel source) {
        super(source);
        source.readParcelable(Location.class.getClassLoader());
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public int getType() {
        return Item.POST;
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
