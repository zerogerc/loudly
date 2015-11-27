package base.attachments;

import android.graphics.Bitmap;
import android.net.Uri;

import base.Networks;

public class Image extends Attachment {
    protected String[] links;
    protected String initialLink;
    protected boolean local;
    protected Bitmap bitmap;


    public Image(String initialLink, String[] links) {
        this.initialLink = initialLink;
        this.links = links;
        this.local = true;
    }

    public Image(String initialLink, boolean local) {
        this.initialLink = initialLink;
        this.local = local;
        this.links = new String[Networks.NETWORK_COUNT];
    }

    public Image(Uri initialLink) {
        this.initialLink = initialLink.toString();
        this.links = new String[Networks.NETWORK_COUNT];
        this.local = true;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setLink(int network, String link) {
        links[network] = link;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public int getType() {
        return Attachment.IMAGE;
    }

    @Override
    public String[] getLinks() {
        return links;
    }

    @Override
    public String getLink(int network) {
        return links[network];
    }

    @Override
    public String getExtra() {
        return initialLink;
    }
}