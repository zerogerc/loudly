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
    protected Link link;

    // Likes, shares, comments
    protected Info info;

    public Say() {
        text = null;
        attachments = new ArrayList<>();
        date = -1;
        info = new Info();
        network = 0;
        link = new Link();
    }

    public Say(String text, int network, Link link) {
        this();
        this.text = text;
        this.network = network;
        this.link = link;
    }

    public Say(String text, long date, int network, Link link) {
        this.text = text;
        this.date = date;
        this.network = network;
        this.attachments = new ArrayList<>();
        this.link = link;
    }

    public Say(String text, ArrayList<Attachment> attachments, long date, int network, Link link) {
        this.text = text;
        this.attachments = attachments;
        this.date = date;
        this.network = network;
        this.info = new Info();
        this.link = link;
    }

    // Methods from SingleNetwork


    @Override
    public boolean exists() {
        return existsIn(network);
    }

    @Override
    public boolean existsIn(int network) {
        return this.network == network && link != null && link.isValid();
    }

    @Override
    public SingleNetwork getNetworkInstance(int network) {
        if (network == this.network) {
            return this;
        }
        return null;
    }

    @Override
    public Link getLink() {
        return link;
    }

    @Override
    public void setLink(Link id) {
        this.link = id;
    }

    @Override
    public int getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(int network) {
        this.network = network;
    }

    /**
     * Return list of attachments. Use only for iterating over attachments
     * @return copy of attachments
     */
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

    public void cleanIds() {
        getLink().setValid(false);
        for (Attachment attachment : attachments) {
            SingleNetwork instance = attachment.getNetworkInstance(getNetwork());
            if (instance != null && instance.getLink() != null) {
                instance.getLink().setValid(false);
            }
        }
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
        if (getDate() < another.getDate()) {
            return -1;
        }
        if (getDate() > another.getDate()) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Say)) {
            return false;
        }
        Say say = (Say)o;
        if (say.getLink() == null) {
            return false;
        }
        return say.getLink().equals(getLink());
    }
}
