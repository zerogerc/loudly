package base.attachments;

public abstract class Attachment {
    public static final int IMAGE = 0;
    protected long localID;

    public abstract int getType();
    public abstract String getExtra();

    public abstract String[] getLinks();
    public abstract void setLink(int network, String link);
    public abstract String getLink(int network);

    public long getLocalID() {
        return localID;
    }

    public void setLocalID(long localID) {
        this.localID = localID;
    }

    public static Attachment makeAttachment(int type, String extra, String[] links) {
        switch (type) {
            case IMAGE:
                return new Image(extra, links);
            default:
                return null;
        }
    }
}
