package ly.loud.loudly.base;


import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {
    public double latitude, longitude;
    public String name;

    public Location(double latitude, double longitude, String name) {
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

}
