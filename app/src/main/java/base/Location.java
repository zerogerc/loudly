package base;

/**
 * Created by Данил on 11/17/2015.
 */
public class Location {
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
}
