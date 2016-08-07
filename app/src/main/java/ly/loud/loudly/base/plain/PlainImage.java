package ly.loud.loudly.base.plain;

import android.graphics.Point;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.interfaces.attachments.Attachment;

/**
 * Just an image
 *
 * @author Danil Kolikov
 */
public class PlainImage implements Attachment {
    @NonNull
    public static final Creator<PlainImage> CREATOR = new Creator<PlainImage>() {
        @Override
        public PlainImage createFromParcel(Parcel parcel) {
            return new PlainImage(parcel);
        }

        @Override
        public PlainImage[] newArray(int i) {
            return new PlainImage[i];
        }
    };

    @Nullable
    private final String url;

    @Nullable
    private final Point size;

    public PlainImage(@Nullable String url, @Nullable Point size) {
        this.url = url;
        this.size = size;
    }

    protected PlainImage(@NonNull Parcel parcel) {
        url = parcel.readString();
        size = parcel.readParcelable(Point.class.getClassLoader());
    }

    @Override
    public int getType() {
        return TYPE_IMAGE;
    }

    @Nullable
    @Override
    public String getExtra() {
        return url;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public Point getSize() {
        return size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeParcelable(size, i);
    }
}
