package ly.loud.loudly.base.says;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;

public class Say implements SingleNetwork {
    // Main part
    protected String text;
    protected ArrayList<Attachment> attachments;
    protected long date;

    // Links part
    protected int network;
    protected Link link;

    // Likes, shares, comments
    protected Info info;

    public static final Comparator<Say> COMPARATOR = new Comparator<Say>() {
        @Override
        public int compare(Say lhs, Say rhs) {
            // Compare says by date
            if (lhs.getDate() < rhs.getDate()) {
                return -1;
            }
            if (lhs.getDate() > rhs.getDate()) {
                return 1;
            }

            if (lhs.getNetwork() == rhs.getNetwork()) {
                return lhs.getLink().compareTo(rhs.getLink());
            }
            // If says are from different networks, compare by network ID
            if (lhs.getNetwork() < rhs.getNetwork()) {
                return -1;
            }
            if (lhs.getNetwork() > rhs.getNetwork()) {
                return 1;
            }
            return 0;
        }
    };

    public static final Comparator<Say> FEED_ORDER = Collections.reverseOrder(COMPARATOR);

    public Say() {
        text = null;
        attachments = new ArrayList<>();
        date = -1;
        info = new Info();
        network = 0;
        link = new Link();
    }

    public Say(String text, int network, Link link) {
        this();
        this.text = text;
        this.network = network;
        this.link = link;
    }

    public Say(String text, long date, int network, Link link) {
        this.text = text;
        this.date = date;
        this.network = network;
        this.attachments = new ArrayList<>();
        this.link = link;
    }

    public Say(String text, ArrayList<Attachment> attachments, long date, int network, Link link) {
        this.text = text;
        this.attachments = attachments;
        this.date = date;
        this.network = network;
        this.info = new Info();
        this.link = link;
    }

    public Say(Parcel source) {
        text = source.readString();
        //TODO: check this
        attachments = source.readArrayList(Image.class.getClassLoader());
        date = source.readLong();
        network = source.readInt();
        link = source.readParcelable(Link.class.getClassLoader());
        info = source.readParcelable(Info.class.getClassLoader());
    }

    // Methods from SingleNetwork


    @Override
    public boolean exists() {
        return existsIn(network);
    }

    @Override
    public boolean existsIn(int network) {
        return this.network == network && link != null && link.isValid();
    }

    @Override
    public SingleNetwork getNetworkInstance(int network) {
        if (network == this.network && link != null) {
            return this;
        }
        return null;
    }

    @Override
    public Link getLink() {
        return link;
    }

    @Override
    public void setLink(Link id) {
        this.link = id;
    }

    @Override
    public int getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(int network) {
        this.network = network;
    }

    /**
     * Return list of attachments. Use only for iterating over attachments
     * @return copy of attachments
     */
    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public Counter getAttachmentsCounter() {
        Counter res = new Counter();
        for (Attachment attachment : attachments) {
            if (attachment instanceof Image) {
                res.imageCount++;
            }
//            if (attachment instanceof Link) {
//                attachmentsCounter.linkCount++;
//            }
        }
        return res;
    }

    public void cleanIds() {
        getLink().setValid(false);
        for (Attachment attachment : attachments) {
            SingleNetwork instance = attachment.getNetworkInstance(getNetwork());
            if (instance != null && instance.getLink() != null) {
                instance.getLink().setValid(false);
            }
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Say)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        SingleNetwork say = ((Say)o).getNetworkInstance(getNetwork());
        return say != null && getLink().equals(say.getLink());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeList(attachments);
        dest.writeLong(date);
        dest.writeInt(network);
        dest.writeParcelable(link, flags);
        dest.writeParcelable(info, flags);
    }

    public static final Creator<Say> CREATOR = new Creator<Say>() {
        @Override
        public Say createFromParcel(Parcel source) {
            return new Say(source);
        }

        @Override
        public Say[] newArray(int size) {
            return new Say[size];
        }
    };
}
