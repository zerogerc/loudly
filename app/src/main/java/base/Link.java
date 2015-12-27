package base;

import android.os.Parcel;
import android.os.Parcelable;

public class Link implements Parcelable {
    private String link;
    private boolean valid;

    public Link() {
        this(null, false);
    }

    public Link(String link) {
        this(link, true);
    }

    public Link(Object link) {
        this(link == null ? null : link.toString());
    }

    public Link(String link, boolean valid) {
        this.link = link;
        this.valid = valid;
    }

    public String get() {
        return link;
    }

    void set(String link) {
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
            return link.equals(o);
        }
        return link.equals(((Link) o).link);
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
    public String toString() {
        return link;
    }
}
