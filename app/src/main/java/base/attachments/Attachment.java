package base.attachments;

public abstract class Attachment {
    public static final int IMAGE = 0;

    public abstract int getType();
    public abstract String[] getLinks();
    public abstract String getExtra();

    public static Attachment makeAttachment(int type, String extra, String[] links) {
        switch (type) {
            case IMAGE:
                return new Image(extra, links);
            default:
                return null;
        }
    }
}
