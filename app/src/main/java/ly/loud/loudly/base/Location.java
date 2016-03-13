package ly.loud.loudly.base;


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
