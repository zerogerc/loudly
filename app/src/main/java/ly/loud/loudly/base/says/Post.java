package ly.loud.loudly.base.says;

import java.util.ArrayList;

import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Location;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.ui.adapter.Item;

public class Post extends Say implements Item {
    protected Location location;

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

    @Override
    public int getType() {
        return Item.POST;
    }
}
