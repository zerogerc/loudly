package base.says;

import java.util.ArrayList;
import java.util.Calendar;

import base.Link;
import base.Location;
import base.MultipleNetwork;
import base.Networks;
import base.attachments.Attachment;

/**
 * Сlass that stores text and attachments to post.
 */
public class LoudlyPost extends Post implements MultipleNetwork {
    private Link[] ids;
    private Info[] infos;

    public LoudlyPost(
            String text,
            ArrayList<Attachment> attachments,
            Link[] ids,
            long date,
            Location location) {

        super(text, attachments, date, location, 0, null);
        this.ids = ids;
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost() {
        super();
        ids = new Link[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost(String text) {
        super(text, 0, null);
        date = Calendar.getInstance().getTimeInMillis() / 1000;
        ids = new Link[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost(String text, long date, Location location) {
        super(text, date, location, 0, null);
    }

    @Override
    public void cleanIds() {
        ids[network] = null;
        for (Attachment attachment : attachments) {
            if (attachment instanceof MultipleNetwork) {
                ((MultipleNetwork) attachment).setId(network, null);
            } else {
                attachment.setId(null);
            }
        }
    }

    public Link[] getIds() {
        return ids;
    }

    @Override
    public Link getId(int network) {
        return ids[network];
    }

    @Override
    public void setId(int network, Link id) {
        ids[network] = id;
        if (id == null) {
            setInfo(network, new Info());   // Delete old info
        }
    }

    @Override
    public Link getId() {
        return getId(network);
    }

    @Override
    public void setId(Link id) {
        setId(network, id);
    }

    @Override
    public void setInfo(int network, Info info) {
        infos[network] = info;
        infos[Networks.LOUDLY] = new Info();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                infos[Networks.LOUDLY].add(infos[i]);
            }
        }
    }

    @Override
    public void setInfo(Info info) {
        setInfo(network, info);
    }

    @Override
    public Info getInfo(int network) {
        return infos[network];
    }

    @Override
    public Info getInfo() {
        return getInfo(network);
    }

    /**
     * @return Networks.LOUDLY
     */
    @Override
    public int getNetwork() {
        return Networks.LOUDLY;
    }

    @Override
    public boolean exists() {
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (existsIn(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsIn(int network) {
        return ids[network] != null && ids[network].isValid();
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof LoudlyPost) {
            return ids[Networks.LOUDLY].equals(((LoudlyPost) o).ids[Networks.LOUDLY]);
        }
        if (o instanceof Post) {
            Post post = (Post)o;
            return ids[post.getNetwork()] != null && ids[post.getNetwork()].equals(post.getId());
        }
        return false;
    }
}
