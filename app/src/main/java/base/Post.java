package base;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import base.attachments.Attachment;
import base.attachments.Image;

/**
 * Сlass that stores text and attachments to post.
 */
public class Post implements Comparable<Post> {
    public static class Counter {
        public int imageCount, linkCount;

        public Counter() {
            imageCount = 0;
            linkCount = 0;
        }
    }

    public static class Info {
        public int like, repost, comment;

        public Info (int like, int repost, int comment) {
            this.like = like;
            this.repost = repost;
            this.comment = comment;
        }

        public Info() {
            like = 0;
            repost = 0;
            comment = 0;
        }

        public void add(Info info) {
            like += info.like;
            repost += info.repost;
            comment += info.comment;
        }
    }

    private String text;
    private ArrayList<Attachment> attachments;
    private String[] links;
    private Info[] infos;
    private Counter counter;
    private long date;
    private Location location;
    private int mainNetwork; // -1 - Loudly, 0,1,... - as in Networks class
    private Info totalInfo;

    private long localId;

    public Post(
            String text,
            ArrayList<Attachment> attachments,
            String[] links,
            long date,
            Location location,
            long localId) {
        this();
        this.text = text;
        this.attachments = attachments;
        this.links = links;
        this.date = date;
        this.location = location;
        this.localId = localId;
    }

    public Post() {
        text = null;
        attachments = new ArrayList<>();
        links = new String[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        counter = new Counter();
        date = -1;
        location = null;
        localId = -1;
    }

    public Post(String text) {
        this();
        this.text = text;
        date = System.currentTimeMillis();
        location = new Location(0, 0, "");
    }

    public Post(String text, long date, Location location) {
        super();
        this.text = text;
        this.date = date;
        this.location = location;
    }

    public String[] getLinks() {
        return links;
    }

    public String getText() {
        return text;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
    public String getLink(int network) { return links[network]; }

    public void setLink(int network, String link) {
        links[network] = link;
        int connectedNetworks = 0;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (getLink(i) != null) {
                connectedNetworks++;
            }
        }
        if (connectedNetworks == 1) {
            mainNetwork = network;
        } else {
            mainNetwork = -1;
        }
    }

    public int getMainNetwork() {
        return mainNetwork;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void addAttachment(Image im) {
        attachments.add(im);
        counter.imageCount++;
    }

    public void setInfo(int network, Info info) {
        infos[network] = info;
        totalInfo = new Info();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                totalInfo.add(infos[i]);
            }
        }
    }

    public Info getTotalInfo() {
        return totalInfo;
    }

    public Info getInfo(int network) {
        return infos[network];
    }

    public ArrayList<Attachment> getAttachments() {
        return (ArrayList<Attachment>)attachments.clone();
    }

    public Counter getCounter() {
        return counter;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public void detachFromNetwork(int network) {
        links[network] = null;
        for (Attachment attachment : attachments) {
            attachment.setLink(network, null);
        }
    }

    @Override
    public int compareTo(@NonNull Post another) {
        if (date < another.date) {
            return -1;
        }
        if (date > another.date) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Post)) {
            return false;
        }
        Post p = (Post) o;
        return localId == p.localId;
    }
}
