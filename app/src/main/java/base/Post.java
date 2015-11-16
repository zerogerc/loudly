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
public class Post implements Comparable<Post>, Writable {
    public class Counter {
        public int imageCount, linkCount;

        public Counter() {
            imageCount = 0;
            linkCount = 0;
        }
    }

    private String text;
    private ArrayList<Attachment> attachments;
    private PostInfo[] infos;
    private Counter counter;
    private long date;

    public Post() {
        this.text = null;
        attachments = new ArrayList<>();
        infos = new PostInfo[Networks.NETWORK_COUNT];
        counter = new Counter();
        date = -1;
    }

    public Post(String text) {
        this();
        this.text = text;
        date = System.currentTimeMillis();
    }

    @Override
    public void writeToFile(FileWrap file) {
        file.writeString(text);
        // TODO: THERE IS NO ATTACHMENTS
        for (PostInfo info : infos) {
            if (info == null) {
                file.writeString("");
            } else {
                file.writeString(info.link);
            }
        }
        file.writeString(Long.toString(date));
    }

    @Override
    public void readFromFile(FileWrap file) throws IOException {
        text = file.readString();
        if (text == null) {
            throw new IOException();
        }
        // TODO: STILL NO ATTACHMENTS
        for (int i = 0; i < infos.length; i++) {
            String id = file.readString();
            if (id == null) {
                continue;
            }
            infos[i] = new PostInfo(id);
        }
        date = Long.parseLong(file.readString());
    }

    public String getText() {
        return text;
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
