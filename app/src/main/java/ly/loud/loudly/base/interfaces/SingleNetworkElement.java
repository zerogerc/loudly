package ly.loud.loudly.base.interfaces;

import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import ly.loud.loudly.base.entities.Link;
import ly.loud.loudly.base.entities.Info;

import static ly.loud.loudly.networks.Networks.*;

/**
 * New interface for elements, that exist in one network
 *
 * @author Danil Kolikov
 */
public interface SingleNetworkElement extends Parcelable, ElementWithInfo {
    /**
     * Get ID of network, where this element exists
     *
     * @return ID of network
     */
    @Network
    int getNetwork();

    /**
     * Get link of this element in network
     *
     * @return Link
     * @see Link
     */
    @NonNull
    Link getLink();

    /**
     * Set new information
     *
     * @param newInfo New information
     */
    @CheckResult
    @NonNull
    SingleNetworkElement setInfo(@NonNull Info newInfo);
}
