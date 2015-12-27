package base.says;

import java.util.ArrayList;

import base.Link;
import base.Location;
import base.SingleNetwork;
import base.attachments.Attachment;
import base.attachments.Image;
import base.attachments.LoudlyImage;

public class Post extends Say {
    protected Location location;

    public void cleanIds() {
        id = null;
        for (Attachment attachment : attachments) {
            attachment.setId(null);
        }
    }

    public Post() {
        super();
    }

    public Post(String text, int network, Link id) {
        super(text, network, id);
        location = null;
    }

    public Post(String text, long date, Location location, int network, Link id) {
        super(text, date, network, id);
        this.location = location;

    }

    public Post(String text, ArrayList<Attachment> attachments,
                long date, Location location, int network, Link id) {
        super(text, attachments, date, network, id);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
