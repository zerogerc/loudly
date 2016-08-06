package ly.loud.loudly.new_base.interfaces;

import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.new_base.Info;

import static ly.loud.loudly.new_base.Networks.*;

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
