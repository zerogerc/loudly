package ly.loud.loudly.new_base;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.util.Equality;

public class Link implements Parcelable, Comparable<Link> {
    @Nullable
    private String link;
    private boolean valid;

    @Deprecated
    public Link() {
        this("", false);
    }

    public Link(@Nullable Object link) {
        if (link == null) {
            this.link = null;
            valid = false;
        } else {
            this.link = link.toString();
            valid = true;
        }
    }

    public Link(@Nullable String link, boolean valid) {
        this.link = link;
        this.valid = valid;
    }

    @Nullable
    public static String getLink(Link link) {
        return link == null ? null : link.isValid() ? link.get() : null;
    }

    @Nullable
    public String get() {
        return link;
    }

    void set(@Nullable String link) {
        this.link = link;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Link)) {
            return Equality.equal(link, o == null ? null : o.toString());
        }
        return Equality.equal(link, ((Link) o).link);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(link);
        dest.writeSerializable(valid);
    }

    public static final Creator<Link> CREATOR = new Creator<Link>() {
        @Override
        public Link createFromParcel(Parcel source) {
            return new Link(source.readString(), ((boolean) source.readSerializable()));
        }

        @Override
        public Link[] newArray(int size) {
            return new Link[size];
        }
    };

    @Override
    public int compareTo(@NonNull Link another) {
        if (link == null && another.link == null) {
            return 0;
        }
        return link.compareTo(another.link);
    }

    @Override
    public String toString() {
        return link;
    }
}
