package ly.loud.loudly.new_base.interfaces;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.base.Networks.Network;
import ly.loud.loudly.base.says.Info;

/**
 * @author Danil Kolikov
 */
public interface MultipleNetworkElement<T extends SingleNetworkElement> extends Parcelable {
    @Nullable
    T getSingleNetworkInstance(@Network int network);

    void setSingleNetworkInstance(@Network int network, @Nullable T instance);

    @NonNull
    Info getInfo();
}
