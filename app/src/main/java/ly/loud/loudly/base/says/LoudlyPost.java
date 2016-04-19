package ly.loud.loudly.base.says;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.Calendar;

import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.Location;
import ly.loud.loudly.base.MultipleNetwork;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.attachments.Attachment;

/**
 * Сlass that stores text and attachments to post.
 */
public class LoudlyPost extends Post implements MultipleNetwork {
    private Link[] links;
    private Info[] infos;

    /**
     * Proxy class that allows thread-safer work with LoudlyPost
     */
    private static class LoudlyPostProxy extends Post {
        private LoudlyPost parent;

        LoudlyPostProxy(LoudlyPost parent, int network) {
            this.parent = parent;
            this.network = network;
        }

        @Override
        public void cleanIds() {
            parent.links[network].setValid(false);
            for (Attachment attachment : parent.attachments) {
                SingleNetwork instance = attachment.getNetworkInstance(network);
                if (instance != null && instance.getLink() != null) {
                    instance.getLink().setValid(false);
                }
            }
        }

        @Override
        public Location getLocation() {
            return parent.getLocation();
        }

        @Override
        public void setLocation(Location location) {
            parent.setLocation(location);
        }

        @Override
        public boolean exists() {
            return existsIn(network);
        }

        @Override
        public boolean existsIn(int network) {
            return this.network == network && parent.links[network] != null &&
                    parent.links[network].isValid();
        }

        @Override
        public SingleNetwork getNetworkInstance(int network) {
            return super.getNetworkInstance(network);
        }

        @Override
        public Link getLink() {
            return parent.getLink(network);
        }

        @Override
        public void setLink(Link id) {
            parent.setLink(network, id);
        }

        @Override
        public int getNetwork() {
            return network;
        }

        @Override
        public void setNetwork(int network) {
            this.network = network;
        }

        @Override
        public ArrayList<Attachment> getAttachments() {
            ArrayList<Attachment> copy = new ArrayList<>();
            for (Attachment attachment : parent.getAttachments()) {
                copy.add(((Attachment) attachment.getNetworkInstance(network)));
            }
            return copy;
        }

        @Override
        public void addAttachment(Attachment attachment) {
            parent.addAttachment(attachment);
        }

        @Override
        public Counter getAttachmentsCounter() {
            return parent.getAttachmentsCounter();
        }

        @Override
        public String getText() {
            return parent.getText();
        }

        @Override
        public void setText(String text) {
            parent.setText(text);
        }

        @Override
        public Info getInfo() {
            return parent.getInfo(network);
        }

        @Override
        public void setInfo(Info info) {
            parent.setInfo(network, info);
        }

        @Override
        public long getDate() {
            return parent.getDate();
        }

        @Override
        public void setDate(long date) {
            parent.setDate(date);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Post)) {
                return false;
            }
            SingleNetwork say = ((Say)o).getNetworkInstance(getNetwork());
            return say != null && getLink().equals(say.getLink());
        }
    }

    public LoudlyPost(
            String text,
            ArrayList<Attachment> attachments,
            Link[] links,
            long date,
            Location location) {

        super(text, attachments, date, location, 0, null);
        this.links = links;
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost() {
        super();
        links = new Link[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost(String text) {
        super(text, 0, null);
        date = Calendar.getInstance().getTimeInMillis() / 1000;
        links = new Link[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost(String text, long date, Location location) {
        super(text, date, location, 0, null);
    }

    public LoudlyPost(Parcel source) {
        super(source);
        source.readTypedArray(links, Link.CREATOR);
        source.readTypedArray(infos, Info.CREATOR);
    }

    @Override
    public SingleNetwork getNetworkInstance(int network) {
        if (network == Networks.LOUDLY) {
            return this;
        }
        if (links[network] != null) {
            return new LoudlyPostProxy(this, network);
        }
        return null;
    }

    public Link[] getLinks() {
        return links;
    }

    @Override
    public Link getLink(int network) {
        return links[network];
    }

    @Override
    public Link getLink() {
        return links[Networks.LOUDLY];
    }

    @Override
    public void setLink(int network, Link link) {
        links[network] = link;
        if (link == null) {
            setInfo(network, new Info());   // Delete old info
        }
    }

    @Override
    public void cleanIds() {
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (links[i] != null) {
                links[i].setValid(false);
            }
            for (Attachment attachment : attachments) {
                if (attachment.getNetworkInstance(i) != null) {
                    attachment.getNetworkInstance(i).getLink().setValid(false);
                }
            }
        }
    }

    @Override
    public synchronized void setInfo(int network, Info info) {
        infos[network] = info;
        infos[Networks.LOUDLY] = new Info();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                infos[Networks.LOUDLY].add(infos[i]);
            }
        }
    }

    @Override
    public Info getInfo() {
        return getInfo(Networks.LOUDLY);
    }

    @Override
    public Info getInfo(int network) {
        return infos[network];
    }

    /**
     * @return Networks.LOUDLY
     */
    @Override
    public int getNetwork() {
        return Networks.LOUDLY;
    }

    @Override
    public boolean exists() {
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (existsIn(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsIn(int network) {
        return links[network] != null && links[network].isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Post)) {
            return false;
        }
        if (o instanceof LoudlyPost) {
            return links[Networks.LOUDLY].equals(((LoudlyPost) o).links[Networks.LOUDLY]);
        }
        Post post = (Post) o;
        return links[post.getNetwork()] != null && links[post.getNetwork()].equals(post.getLink());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedArray(links, flags);
        dest.writeTypedArray(infos, flags);
    }

    public static final Creator<LoudlyPost> CREATOR = new Creator<LoudlyPost>() {
        @Override
        public LoudlyPost createFromParcel(Parcel source) {
            return new LoudlyPost(source);
        }

        @Override
        public LoudlyPost[] newArray(int size) {
            return new LoudlyPost[size];
        }
    };

}
