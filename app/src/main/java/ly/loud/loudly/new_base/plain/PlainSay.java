package ly.loud.loudly.new_base.plain;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;

import java.util.ArrayList;

/**
 * @author Danil Kolikov
 */
public class PlainSay<T extends Attachment> implements Parcelable {
    public static final Creator<PlainSay> CREATOR = new Creator<PlainSay>() {
        @Override
        public PlainSay createFromParcel(Parcel source) {
            return new PlainSay(source);
        }

        @Override
        public PlainSay[] newArray(int size) {
            return new PlainSay[size];
        }
    };

    @Nullable
    private final String text;

    private final long date;

    @NonNull
    private final ArrayList<T> attachments;

    public PlainSay(@Nullable String text, long date, @NonNull ArrayList<T> attachments) {
        this.text = text;
        this.date = date;
        this.attachments = attachments;
    }

    protected PlainSay(Parcel parcel) {
        text = parcel.readString();
        //TODO: check this
        attachments = parcel.readArrayList(getClass().getClassLoader());
        date = parcel.readLong();
    }

    @Nullable
    public String getText() {
        return text;
    }

    public long getDate() {
        return date;
    }

    @NonNull
    public ArrayList<T> getAttachments() {
        return attachments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeList(attachments);
        parcel.writeLong(date);
    }
}
