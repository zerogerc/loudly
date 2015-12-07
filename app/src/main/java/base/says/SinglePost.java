package base.says;

import java.util.ArrayList;

import base.Location;
import base.attachments.Attachment;

public class SinglePost extends Post {
    String link;

    public SinglePost(String text, long date, Location location, int network, String link) {
        super(text, date, location, network);
        this.link = link;
    }

    public SinglePost(String text, ArrayList<Attachment> attachments, long date, Location location,
                      int network, String link) {
        super(text, attachments, date, location, network);
        this.link = link;
    }

    @Override
    public String getLink(int network) {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean existsIn(int network) {
        return network == this.network;
    }

    @Override
    public void detachFromNetwork(int network) {
        link = null;
        for (Attachment attachment : attachments) {
            attachment.setLink(network, null);
        }
    }
}
