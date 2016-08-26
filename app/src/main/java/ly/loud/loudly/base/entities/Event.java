package ly.loud.loudly.base.entities;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.networks.Networks.Network;

/**
 * Event is new like, share or comment
 */
public class Event {
    @IntDef({LIKE, SHARE, COMMENT})
    public @interface EventType {
    }
    public static final short LIKE = 0;
    public static final short SHARE = 1;
    public static final short COMMENT = 2;

    @EventType
    public final short type;

    @Network
    public final int network;

    @NonNull
    public final LoudlyPost post;

    public final long date;

    public Event(@EventType short type,
                 int network,
                 @NonNull LoudlyPost post,
                 long date) {
        this.type = type;
        this.network = network;
        this.post = post;
        this.date = date;
    }

    @NonNull
    public Info toInfo() {
        switch (type) {
            case LIKE:
                return new Info(1, 0, 0);
            case SHARE:
                return new Info(0, 1, 0);
            default:    // Comment
                return new Info(0, 0, 1);
        }
    }
}
