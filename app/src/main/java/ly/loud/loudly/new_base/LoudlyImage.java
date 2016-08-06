package ly.loud.loudly.new_base;

import android.graphics.Point;
import android.os.Parcel;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.interfaces.attachments.LocalFile;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainImage;

import java.io.File;
import java.util.ArrayList;


/**
 * Image from multiple networks
 *
 * @author Danil Kolikov
 */
public class LoudlyImage extends PlainImage implements MultipleAttachment, LocalFile {
    @NonNull
    public static final Creator<LoudlyImage> CREATOR = new Creator<LoudlyImage>() {
        @Override
        public LoudlyImage createFromParcel(Parcel parcel) {
            return new LoudlyImage(parcel);
        }

        @Override
        public LoudlyImage[] newArray(int i) {
            return new LoudlyImage[i];
        }
    };

    @NonNull
    private final SingleImage[] elements;

    public LoudlyImage(@Nullable String url, @Nullable Point size, @NonNull SingleImage[] elements) {
        super(url, size);
        this.elements = elements;
    }

    public LoudlyImage(@Nullable String url, @Nullable Point size) {
        this(url, size,  new SingleImage[Networks.NETWORK_COUNT]);
    }

    private LoudlyImage(@NonNull Parcel source) {
        super(source);
        elements = source.createTypedArray(SingleImage.CREATOR);
    }

    @Nullable
    @Override
    public SingleImage getSingleNetworkInstance(@Network int network) {
        return elements[network];
    }

    @CheckResult
    @NonNull
    @Override
    public LoudlyImage setSingleNetworkInstance(@Network int network, @Nullable SingleAttachment instance) {
        SingleImage[] copied = new SingleImage[elements.length];
        System.arraycopy(elements, 0, copied, 0, elements.length);

        if (instance == null) {
            copied[network] = null;
            return new LoudlyImage(getUrl(), getSize(), copied);
        }
        if (instance.getType() == TYPE_IMAGE) {
            copied[network] = (SingleImage) instance;
            return new LoudlyImage(getUrl(), getSize(), copied);
        }
        // Impossible
        return this;
    }

    @NonNull
    @Override
    public ArrayList<SingleAttachment> getNetworkInstances() {
        ArrayList<SingleAttachment> instances = new ArrayList<>();
        for (SingleAttachment attachment : elements) {
            if (attachment != null) {
                instances.add(attachment);
            }
        }
        return instances;
    }

    @NonNull
    @Override
    public Info getInfo() {
        Info info = new Info();
        for (SingleImage image : elements) {
            if (image != null) {
                info.add(image.getInfo());
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

    @Nullable
    @Override
    public File getFile() {
        // ToDo: extract file from local path
        return null;
    }
}
