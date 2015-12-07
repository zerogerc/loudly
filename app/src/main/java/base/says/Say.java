package base.says;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import base.attachments.Attachment;
import base.attachments.Image;

public abstract class Say implements Comparable<Say> {
    protected String text;
    protected ArrayList<Attachment> attachments;
    protected long date;
    protected int network;

    protected Info info;

    public Say() {
        text = "";
        attachments = new ArrayList<>();
        date = -1;
        info = new Info();
        network = -1;
    }

    public Say(String text, int network) {
        this();
        this.text = text;
        this.network = network;
    }

    public Say(String text, long date, int network) {
        this.text = text;
        this.date = date;
        this.network = network;
        this.attachments = new ArrayList<>();
    }

    public Say(String text, ArrayList<Attachment> attachments, long date, int network) {
        this.text = text;
        this.attachments = attachments;
        this.date = date;
        this.network = network;
        this.info = new Info();
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

    public int getNetwork() {
        return network;
    }

    public void setNetwork(int network) {
        this.network = network;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
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
        return o instanceof Say && ((Say) o).date == date;
    }
}
