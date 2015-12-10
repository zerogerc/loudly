package base.says;

import java.util.ArrayList;
import java.util.Calendar;

import base.Location;
import base.Networks;
import base.attachments.Attachment;
import util.Network;

/**
 * Сlass that stores text and attachments to post.
 */
public class LoudlyPost extends Post {
    private String[] links;
    private Info[] infos;

    private long localId;

    public LoudlyPost(
            String text,
            ArrayList<Attachment> attachments,
            String[] links,
            long date,
            Location location,
            long localId) {

        super(text, attachments, date, location, -1);
        this.links = links;
        this.localId = localId;
        infos = new Info[Networks.NETWORK_COUNT];
    }

    public LoudlyPost() {
        super();
        links = new String[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        localId = -1;
    }

    public LoudlyPost(String text) {
        super(text, -1);
        date = Calendar.getInstance().getTimeInMillis() / 1000;
        links = new String[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        localId = -1;
    }

    public LoudlyPost(String text, long date, Location location) {
        super(text, date, location, -1);
    }

    public String[] getLinks() {
        return links;
    }

    @Override
    public String getLink(int network) {
        return links[network];
    }

    public void setLink(int network, String link) {
        links[network] = link;
    }

    @Override
    public boolean existsIn(int network) {
        return links[network] != null;
    }

    public void setInfo(int network, Info info) {
        infos[network] = info;
        this.info = new Info();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                this.info.add(infos[i]);
            }
        }
    }

    public Info getInfo(int network) {
        return infos[network];
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    @Override
    public void detachFromNetwork(int network) {
        links[network] = null;
        for (Attachment attachment : attachments) {
            attachment.setLink(network, null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoudlyPost)) {
            return false;
        }
        LoudlyPost p = (LoudlyPost) o;
        return localId == p.localId;
    }
}
