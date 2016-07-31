package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.new_base.plain.PlainPost;

import java.util.ArrayList;

/**
 * @author Danil Kolikov
 */
public class LoudlyPost extends PlainPost<MultipleAttachment>
        implements MultipleNetworkElement<SinglePost> {
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

    @NonNull
    private final SinglePost[] elements;

    public LoudlyPost(@Nullable String text, long date,
                      @NonNull ArrayList<MultipleAttachment> attachments,
                      @Nullable Location location) {
        super(text, date, attachments, location);
        elements = new SinglePost[Networks.NETWORK_COUNT];
    }

    private LoudlyPost(Parcel source) {
        super(source);
        elements = source.createTypedArray(SinglePost.CREATOR);
    }

    @Nullable
    @Override
    public SinglePost getSingleNetworkInstance(@Networks.Network int network) {
        return elements[network];
    }

    @Override
    public void setSingleNetworkInstance(@Networks.Network int network, @Nullable SinglePost instance) {
        // Now single post is always post
        elements[network] = instance;
    }

    @NonNull
    @Override
    public ArrayList<SinglePost> getNetworkInstances() {
        ArrayList<SinglePost> instances = new ArrayList<>();
        for (SinglePost post : elements) {
            if (post != null) {
                instances.add(post);
            }
        }
        return instances;
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
        super.writeToParcel(parcel, i);
        parcel.writeTypedArray(elements, i);
    }
}
