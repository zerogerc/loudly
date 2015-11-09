package base;

import java.util.ArrayList;

import base.attachments.Attachable;
import base.attachments.Image;
import base.attachments.Link;
import base.attachments.Video;
import util.Counter;

public class Post {
    private String text;
    private ArrayList<Attachable> attachments;
    private int imageCount, linkCount, videoCount;

    public Post(String text) {
        this.text = text;
        attachments = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public void addAttachment(Image im) {
        attachments.add(im);
        imageCount++;
    }

    public void addAttachment(Video v) {
        attachments.add(v);
        videoCount++;
    }

    public void addAttachment(Link l) {
        attachments.add(l);
        linkCount++;
    }

    public ArrayList<Attachable> getAttachments() {
        return (ArrayList<Attachable>)attachments.clone();
    }

    public Counter getCounter() {
        return new Counter(text.length(), imageCount, linkCount, videoCount);
    }
}
