package base.says;

import java.util.ArrayList;

import base.Location;
import base.attachments.Attachment;

public abstract class Post extends Say {
    protected Location location;

    public abstract String getLink(int network);
    public abstract void detachFromNetwork(int network);
    public abstract boolean existsIn(int network);

    public Post() {
        super();
    }

    public Post(String text, int network) {
        super(text, network);
        location = null;
    }

    public Post(String text, long date, Location location, int network) {
        super(text, date, network);
        this.location = location;
    }

    public Post(String text, ArrayList<Attachment> attachments, long date, Location location, int network) {
        super(text, attachments, date, network);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
