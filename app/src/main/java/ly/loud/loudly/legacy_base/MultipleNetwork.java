package ly.loud.loudly.legacy_base;

import android.os.Parcelable;
import ly.loud.loudly.base.entities.Info;

/**
 * Interface for objects which are in many networks. They are stored in the local database
 */

public interface MultipleNetwork extends Parcelable {
    Link getLink(int network);
    void setLink(int network, Link link);
    Link[] getLinks();

    Info getInfo(int network);
    void setInfo(int network, Info info);
}
