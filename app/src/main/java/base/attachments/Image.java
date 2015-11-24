package base.attachments;

import android.net.Uri;

import base.Networks;

public class Image extends Attachment {
    protected String[] links;
    protected String initialLink;

    public Image(String initialLink, String[] links) {
        this.initialLink = initialLink;
        this.links = links;
    }

    public Image(Uri initialLink) {
        this.initialLink = initialLink.toString();
        this.links = new String[Networks.NETWORK_COUNT];
    }

    public void setLink(int network, String link) {
        links[network] = link;
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