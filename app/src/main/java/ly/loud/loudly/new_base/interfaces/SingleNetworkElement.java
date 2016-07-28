package ly.loud.loudly.new_base.interfaces;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import ly.loud.loudly.base.Link;
import ly.loud.loudly.base.says.Info;

import static ly.loud.loudly.base.Networks.*;

/**
 * New interface for elements, that exist in one network
 *
 * @author Danil Kolikov
 */
public interface SingleNetworkElement extends Parcelable {
    @Network
    int getNetwork();

    @NonNull
    Link getLink();

    @NonNull
    Info getInfo();

    void setInfo(@NonNull Info newInfo);
}
