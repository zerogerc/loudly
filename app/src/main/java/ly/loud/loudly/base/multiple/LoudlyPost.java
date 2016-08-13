package ly.loud.loudly.base.multiple;

import android.os.Parcel;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Location;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;

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

    /**
     * Set instance to this post and all it's
     *
     * @param instance New instance (may be null)
     * @return
     */
    @CheckResult
    @NonNull
    @Override
    public LoudlyPost setSingleNetworkInstance(@NonNull SinglePost instance) {
        if (instance.getAttachments().size() != getAttachments().size()) {
            throw new IllegalArgumentException("Wrong size of attachments");
        }
        SinglePost[] copied = new SinglePost[elements.length];
        System.arraycopy(elements, 0, copied, 0, copied.length);
        copied[instance.getNetwork()] = instance;
        ArrayList<MultipleAttachment> newAttachments = new ArrayList<>();
        for (int i = 0; i < getAttachments().size(); i++) {
            MultipleAttachment multipleAttachment = getAttachments().get(i);
            SingleAttachment singleAttachment = instance.getAttachments().get(i);

            newAttachments.add(
                    (MultipleAttachment) multipleAttachment
                            .setSingleNetworkInstance(singleAttachment)
            );
        }
        return new LoudlyPost(getText(), getDate(), newAttachments, getLocation(), copied);
    }

    @NonNull
    @Override
    public LoudlyPost deleteNetworkInstance(@Network int network) {
        SinglePost[] copied = new SinglePost[elements.length];
        System.arraycopy(elements, 0, copied, 0, copied.length);
        copied[network] = null;
        ArrayList<MultipleAttachment> newAttachments = new ArrayList<>();
        for (MultipleAttachment attachment : getAttachments()) {
            newAttachments.add((MultipleAttachment) attachment.deleteNetworkInstance(network));
        }
        return new LoudlyPost(getText(), getDate(), newAttachments, getLocation(), copied);
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
