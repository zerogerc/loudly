package ly.loud.loudly.base.interfaces;

import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import ly.loud.loudly.networks.Networks.Network;

/**
 * Interface for element, that exists in many networks
 *
 * @param <T> type of instances of this element in networks
 * @author Danil Kolikov
 */
public interface MultipleNetworkElement<T extends SingleNetworkElement> extends ElementWithInfo, Parcelable {
    /**
     * Get instance of this element in networks
     *
     * @param network ID of network
     * @return Instance (may be null)
     */
    @Nullable
    T getSingleNetworkInstance(@Network int network);

    /**
     * Set new network instance
     *
     * @param instance New instance
     * @return New MultipleNetworkElement with instance set
     */
    @CheckResult
    @NonNull
    MultipleNetworkElement<T> setSingleNetworkInstance(@NonNull T instance);

    /**
     * Delete instance in some network
     *
     * @param network ID of network
     * @return New MultipleNetworkElement with instance deleted
     */
    @CheckResult
    @NonNull
    MultipleNetworkElement<T> deleteNetworkInstance(@Network int network);

    /**
     * Get stream of all network instances
     *
     * @return Observable
     */
    @NonNull
    ArrayList<T> getNetworkInstances();
}
