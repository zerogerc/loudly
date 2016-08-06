package ly.loud.loudly.base.interfaces;

import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.networks.Networks.Network;

import java.util.ArrayList;

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
     * @param network ID of network
     * @param instance New instance (may be null)
     */
    @CheckResult
    @NonNull
    MultipleNetworkElement<T> setSingleNetworkInstance(@Network int network, @Nullable T instance);

    /**
     * Get stream of all network instances
     *
     * @return Observable
     */
    @NonNull
    ArrayList<T> getNetworkInstances();
}
