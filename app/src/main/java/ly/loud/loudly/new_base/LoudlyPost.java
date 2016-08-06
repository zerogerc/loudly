package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.new_base.plain.PlainPost;

/**
 * @author Danil Kolikov
 */
public class LoudlyPost extends PlainPost<MultipleAttachment>
        implements MultipleNetworkElement<SinglePost> {
    @NonNull
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

    public LoudlyPost(@Nullable String text,
                      long date,
                      @NonNull ArrayList<MultipleAttachment> attachments,
                      @Nullable Location location,
                      @NonNull SinglePost[] elements) {
        super(text, date, attachments, location);
        this.elements = elements;
    }

    public LoudlyPost(@Nullable String text,
                      long date,
                      @NonNull ArrayList<MultipleAttachment> attachments,
                      @Nullable Location location) {
        this(text, date, attachments, location, new SinglePost[Networks.NETWORK_COUNT]);
    }

    private LoudlyPost(@NonNull Parcel source) {
        super(source);
        elements = source.createTypedArray(SinglePost.CREATOR);
    }

    @Nullable
    @Override
    public SinglePost getSingleNetworkInstance(@Network int network) {
        return elements[network];
    }

    @CheckResult
    @NonNull
    @Override
    public LoudlyPost setSingleNetworkInstance(@Network int network, @Nullable SinglePost instance) {
        // Now single post is always post
        SinglePost[] copied = new SinglePost[elements.length];
        System.arraycopy(elements, 0, copied, 0, copied.length);
        copied[network] = instance;
        return new LoudlyPost(getText(), getDate(), getAttachments(), getLocation(), copied);
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
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeTypedArray(elements, i);
    }
}
