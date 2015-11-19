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
    }

    private String text;
    private ArrayList<Attachment> attachments;
    private String[] links;
    private Info[] infos;
    private Counter counter;
    private long date;
    private Location location;

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

    public String getLink(int network) { return links[network]; }
    public void setLink(int network, String link) { links[network] = link; }

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
}
