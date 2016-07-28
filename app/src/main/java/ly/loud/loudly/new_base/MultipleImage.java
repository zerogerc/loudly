package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.new_base.interfaces.attachments.LocalFile;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;

import java.io.File;


/**
 * Image from multiple networks
 *
 * @author Danil Kolikov
 */
public class MultipleImage implements MultipleAttachment, LocalFile {
    public static final Creator<MultipleImage> CREATOR = new Creator<MultipleImage>() {
        @Override
        public MultipleImage createFromParcel(Parcel parcel) {
            return new MultipleImage(parcel);
        }

        @Override
        public MultipleImage[] newArray(int i) {
            return new MultipleImage[i];
        }
    };

    @NonNull
    private Image[] elements;
    /**
     * Path to local file (uri)
     */
    @Nullable
    private String localPath;

    public MultipleImage(@Nullable String localPath) {
        elements = new Image[Networks.NETWORK_COUNT];
        this.localPath = localPath;
    }

    private MultipleImage(Parcel source) {
        elements = source.createTypedArray(Image.CREATOR);
        localPath = source.readString();
    }

    @Nullable
    @Override
    public Image getSingleNetworkInstance(@Networks.Network int network) {
        return elements[network];
    }

    @Override
    public void setSingleNetworkInstance(@Networks.Network int network, @Nullable SingleAttachment instance) {
        if (instance == null) {
            elements[network] = null;
            return;
        }
        if (instance.getType() == TYPE_IMAGE) {
            elements[network] = (Image) instance;
        }
    }

    @NonNull
    @Override
    public Info getInfo() {
        Info info = new Info();
        for (Image image : elements) {
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localPath);
        parcel.writeTypedArray(elements, i);
    }

    @Override
    public int getType() {
        return TYPE_IMAGE;
    }

    @Nullable
    @Override
    public String getExtra() {
        return localPath;
    }

    @Nullable
    @Override
    public File getFile() {
        // ToDo: extract file from local path
        return null;
    }
}
