package ly.loud.loudly.new_base.plain;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ly.loud.loudly.new_base.Location;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;
import ly.loud.loudly.ui.adapter.Item;

/**
 * Just post - say + location
 * @author Danil Kolikov
 */
public class PlainPost<T extends Attachment> extends PlainSay<T> implements Item {
    @NonNull
    public static final Creator<PlainPost> CREATOR = new Creator<PlainPost>() {
        @Override
        public PlainPost createFromParcel(Parcel parcel) {
            return new PlainPost(parcel);
        }

        @Override
        public PlainPost[] newArray(int i) {
            return new PlainPost[i];
        }
    };
    @Nullable
    private final Location location;

    public PlainPost(@Nullable String text,
                     long date,
                     @NonNull ArrayList<T> attachments,
                     @Nullable Location location) {
        super(text, date, attachments);
        this.location = location;
    }

    protected PlainPost(@NonNull Parcel parcel) {
        super(parcel);
        location = parcel.readParcelable(Location.class.getClassLoader());
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    @Override
    public int getType() {
        return POST;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(location, i);
    }
}
