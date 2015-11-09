package base.attachments;

public abstract class Image implements Uploadable {
    protected static String[] links;
    protected String initialLink;

    public Image(String initialLink) {
        this.initialLink = initialLink;
    }

    protected static void setLink(int network, String link) {
        links[network] = link;
    }
}
