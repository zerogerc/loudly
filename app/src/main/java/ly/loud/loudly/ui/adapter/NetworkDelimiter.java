package ly.loud.loudly.ui.adapter;

import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.ui.brand_new.ItemTypes.ItemType;
import ly.loud.loudly.ui.brand_new.adapter.ListItem;

import static ly.loud.loudly.ui.brand_new.ItemTypes.DELIMITER;

/**
 * Created by ZeRoGerc on 07.12.15.
 * ITMO University
 */

public class NetworkDelimiter implements ListItem {
    @Network
    private int network;

    public NetworkDelimiter() {
        this.network = -1;
    }

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
