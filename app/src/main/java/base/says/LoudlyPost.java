package base.says;

import java.util.ArrayList;
import java.util.Calendar;

import base.Location;
import base.MultipleNetwork;
import base.Networks;
import base.attachments.Attachment;

/**
 * Сlass that stores text and attachments to post.
 */
public class LoudlyPost extends Post implements MultipleNetwork {
    private String[] ids;
    private Info[] infos;

    private long localId;       // Convert to string for sameness

    public LoudlyPost(
            String text,
            ArrayList<Attachment> attachments,
            String[] ids,
            long date,
            Location location,
            long localId) {

        super(text, attachments, date, location, -1, "");
        this.ids = ids;
        this.localId = localId;
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
    }

    public LoudlyPost() {
        super();
        ids = new String[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
        localId = -1;
    }

    public LoudlyPost(String text) {
        super(text, -1, "");
        date = Calendar.getInstance().getTimeInMillis() / 1000;
        ids = new String[Networks.NETWORK_COUNT];
        infos = new Info[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            infos[i] = new Info();
        }
        localId = -1;
    }

    public LoudlyPost(String text, long date, Location location) {
        super(text, date, location, -1, "");
    }

    @Override
    public void cleanIds() {
        ids[network] = null;
        for (Attachment attachment : attachments) {
            if (attachment instanceof MultipleNetwork) {
                ((MultipleNetwork) attachment).setId(network, null);
            } else {
                attachment.setId(null);
            }
        }
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public String[] getIds() {
        return ids;
    }

    @Override
    public String getId(int network) {
        return ids[network];
    }

    @Override
    public void setId(int network, String link) {
        ids[network] = link;
    }

    @Override
    public String getId() {
        return ids[network];
    }

    @Override
    public void setId(String id) {
        ids[network] = id;
    }

    @Override
    public void setInfo(int network, Info info) {
        infos[network] = info;
        this.info = new Info();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (infos[i] != null) {
                this.info.add(infos[i]);
            }
        }
    }

    @Override
    public Info getInfo(int network) {
        return infos[network];
    }

    @Override
    public boolean existsIn(int network) {
        return ids[network] != null;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoudlyPost)) {
            return false;
        }
        LoudlyPost p = (LoudlyPost) o;
        return localId == p.localId;
    }
}
