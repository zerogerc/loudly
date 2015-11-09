package base.attachments;

import base.Networks;
import util.Parameter;

public class Image extends Uploadable {
    protected static String[] links;
    protected String initialLink;

    public Image(String initialLink) {
        this.initialLink = initialLink;
        links = new String[Networks.NETWORK_COUNT];
    }

    @Override
    public void upload() {
        throw new NoSuchMethodError();
    }

    @Override
    public Parameter toParameter() {
        throw new NoSuchMethodError();
    }

    protected static void setLink(int network, String link) {
        links[network] = link;
    }
}
