package ly.loud.loudly.ui.adapters;

import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import ly.loud.loudly.ui.adapters.holders.ListItem;

import static ly.loud.loudly.ui.adapters.holders.ItemTypes.DELIMITER;

public class NetworkDelimiter implements ListItem {
    @Network
    private final int network;

    public NetworkDelimiter(@Network int network) {
        this.network = network;
    }

    @Network
    public int getNetwork() {
        return network;
    }

    @Override
    @ItemType
    public int getType() {
        return DELIMITER;
    }
}
