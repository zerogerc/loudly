package base.says;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import base.Link;
import base.SingleNetwork;
import base.attachments.Attachment;
import base.attachments.Image;

public class Say implements SingleNetwork, Comparable<Say> {
    // Main part
    protected String text;
    protected ArrayList<Attachment> attachments;
    protected long date;

    // Links part
    protected int network;
    protected Link id;

    // Likes, shares, comments
    protected Info info;

    public Say() {
        text = null;
        attachments = new ArrayList<>();
        date = -1;
        info = new Info();
        network = -1;
        id = new Link();
    }

    public Say(String text, int network, Link id) {
        this();
        this.text = text;
        this.network = network;
        this.id = id;
    }

    public Say(String text, long date, int network, Link id) {
        this.text = text;
        this.date = date;
        this.network = network;
        this.attachments = new ArrayList<>();
        this.id = id;
    }

    public Say(String text, ArrayList<Attachment> attachments, long date, int network, Link id) {
        this.text = text;
        this.attachments = attachments;
        this.date = date;
        this.network = network;
        this.info = new Info();
        this.id = id;
    }

    // Methods from SingleNetwork


    @Override
    public boolean exists() {
        return existsIn(network);
    }

    @Override
    public boolean existsIn(int network) {
        return this.network == network && id != null && id.isValid();
    }

    @Override
    public Link getId() {
        return id;
    }

    @Override
    public void setId(Link id) {
        this.id = id;
    }

    @Override
    public int getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(int network) {
        this.network = network;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public Counter getAttachmentsCounter() {
        Counter res = new Counter();
        for (Attachment attachment : attachments) {
            if (attachment instanceof Image) {
                res.imageCount++;
            }
//            if (attachment instanceof Link) {
//                attachmentsCounter.linkCount++;
//            }
        }
        return res;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int compareTo(@NonNull Say another) {
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
        return o instanceof Say && ((Say) o).id.equals(id);
    }
}
