package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Location;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.interfaces.says.MultiplePost;
import ly.loud.loudly.new_base.interfaces.says.SinglePost;

import java.util.ArrayList;

/**
 * @author Danil Kolikov
 */
public class LoudlyPost implements MultiplePost {
    @NonNull
    private Post[] elements;
    @Nullable
    private String text;
    private long date;
    @NonNull
    private ArrayList<MultipleAttachment> attachments;
    @Nullable
    private Location location;

    public LoudlyPost(@Nullable String text, long date,
                      @NonNull ArrayList<MultipleAttachment> attachments,
                      @Nullable Location location) {
        this.text = text;
        this.date = date;
        this.attachments = attachments;
        this.location = location;
        elements = new Post[Networks.NETWORK_COUNT];
    }

    private LoudlyPost(Parcel source) {
        text = source.readString();
        date = source.readLong();
        attachments = source.readArrayList(getClass().getClassLoader());
        location = Location.CREATOR.createFromParcel(source);
        elements = source.createTypedArray(Post.CREATOR);
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Nullable
    @Override
    public Post getSingleNetworkInstance(@Networks.Network int network) {
        return elements[network];
    }

    @Override
    public void setSingleNetworkInstance(@Networks.Network int network, @Nullable SinglePost instance) {
        // Now single post is always post
        elements[network] = (Post)instance;
    }

    @Nullable
    @Override
    public Location getLocation() {
        return location;
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
    public ArrayList<MultipleAttachment> getAttachments() {
        return attachments;
    }

    @NonNull
    @Override
    public Info getInfo() {
        Info info = new Info();
        for (SinglePost post : elements) {
            if (post != null) {
                info.add(post.getInfo());
            }
        }
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeLong(date);
        parcel.writeList(attachments);
        parcel.writeParcelable(location, i);
        parcel.writeTypedArray(elements, i);
    }

    public static final Creator<LoudlyPost> CREATOR = new Creator<LoudlyPost>() {
        @Override
        public LoudlyPost createFromParcel(Parcel parcel) {
            return new LoudlyPost(parcel);
        }

        @Override
        public LoudlyPost[] newArray(int i) {
            return new LoudlyPost[i];
        }
    };
}
