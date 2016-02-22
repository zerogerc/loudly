package base.says;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;

import base.Link;
import base.Location;
import base.MultipleNetwork;
import base.Networks;
import base.SingleNetwork;
import base.attachments.Attachment;

/**
 * Сlass that stores text and attachments to post.
 */
public class LoudlyPost extends Post implements MultipleNetwork {
    private Link[] links;
    private Info[] infos;

    /**
     * Proxy class that allows thread-safer work with LoudlyPost
     */
    private class LoudlyPostProxy extends Post {
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
        public int compareTo(@NonNull Say another) {
            return parent.compareTo(another);
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
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

    @Override
    public SingleNetwork getNetworkInstance(int network) {
        if (network == Networks.LOUDLY) {
            return this;
        }
        return new LoudlyPostProxy(this, network);
    }

    public Link[] getLinks() {
        return links;
    }

    @Override
    public Link getLink(int network) {
        return links[network];
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
    public void setInfo(int network, Info info) {
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
        if (o instanceof LoudlyPost) {
            return links[Networks.LOUDLY].equals(((LoudlyPost) o).links[Networks.LOUDLY]);
        }
        if (o instanceof Post) {
            Post post = (Post) o;
            return links[post.getNetwork()] != null && links[post.getNetwork()].equals(post.getLink());
        }
        return false;
    }
}
