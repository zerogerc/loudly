package base.attachments;

public abstract class Attachment {
    public static final int IMAGE = 0;

    public abstract int getType();
    public abstract String[] getLinks();
}
