package base;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import base.attachments.Attachment;
import base.attachments.Image;
import base.attachments.Link;
import util.FileWrap;
import util.Network;
import util.Writable;

/**
 * Сlass that stores text and attachments to post.
 */
public class Post implements Comparable<Post> {
    public class Counter {
        public int imageCount, linkCount;

        public Counter() {
            imageCount = 0;
            linkCount = 0;
        }
    }

    private String text;
    private ArrayList<Attachment> attachments;
    private String[] links;
    private PostInfo[] infos;
    private Counter counter;
    private long date;
    private Location location;

    public Post() {
        text = null;
        attachments = new ArrayList<>();
        links = new String[Networks.NETWORK_COUNT];
        infos = new PostInfo[Networks.NETWORK_COUNT];
        counter = new Counter();
        date = -1;
        location = null;
    }

    public Post(String text) {
        this();
        this.text = text;
        date = System.currentTimeMillis();
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

    public void addAttachment(Link l) {
        attachments.add(l);
        counter.linkCount++;
    }

    public void setInfo(int network, PostInfo link) {
        infos[network] = link;
    }


    public PostInfo getInfo(int network) {
        return infos[network];
    }

    public ArrayList<Attachment> getAttachments() {
        return (ArrayList<Attachment>)attachments.clone();
    }

    public Counter getCounter() {
        return counter;
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
