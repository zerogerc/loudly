package ly.loud.loudly.new_base;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class Location implements Parcelable {
    public final double latitude, longitude;

    @Nullable
    public String name;

    public Location(double latitude, double longitude, @Nullable String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public Location(double latitude, double longitude) {
        this(latitude, longitude, null);
    }

    public Location(Parcel source) {
        latitude = source.readDouble();
        longitude = source.readDouble();
        name = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(name);
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location casted = ((Location) obj);
        return latitude == casted.latitude && longitude == casted.longitude &&
                (name == null && casted.name == null || name != null && name.equals(((Location) obj).name));
    }
}
