package base.attachments;

import base.Networks;
import util.Parameter;

public class Image extends Attachment {
    protected static String[] links;
    protected String initialLink;

    public Image(String initialLink) {
        this.initialLink = initialLink;
        links = new String[Networks.NETWORK_COUNT];
    }

    protected static void setLink(int network, String link) {
        links[network] = link;
    }
    protected static String getLink(int network) { return links[network]; }
}
