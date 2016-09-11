package ly.loud.loudly.ui.sidebar;

import android.support.annotation.NonNull;

import ly.loud.loudly.networks.NetworkContract;
import solid.collections.SolidList;

public interface SideBarView {
    public void onNetworksLoaded(@NonNull SolidList<NetworkContract> networkContracts);
}
