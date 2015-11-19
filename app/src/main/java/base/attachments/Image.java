package base.attachments;

import base.Networks;
import util.Parameter;

public class Image extends Attachment {
    protected String[] links;
    protected String initialLink;

    public Image(String initialLink, String[] links) {
        this.initialLink = initialLink;
        this.links = links;
    }

    protected void setLink(int network, String link) {
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
    public String getExtra() {
        return initialLink;
    }
}