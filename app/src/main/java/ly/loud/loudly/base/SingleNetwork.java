package ly.loud.loudly.base;

import android.os.Parcelable;
import ly.loud.loudly.new_base.Info;
import ly.loud.loudly.new_base.Link;

/**
 * Interface for objects which are in some network
 */
public interface SingleNetwork extends Parcelable {
    void setNetwork(int network);
    int getNetwork();

    boolean exists();
    boolean existsIn(int network);

    SingleNetwork getNetworkInstance(int network);

    Link getLink();
    void setLink(Link id);

    Info getInfo();
    void setInfo(Info info);
}
