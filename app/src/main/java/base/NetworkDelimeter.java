package base;

import ly.loud.loudly.PeopleList.Item;

/**
 * Created by ZeRoGerc on 07.12.15.
 */
public class NetworkDelimeter extends Item {
    private int network;

    public NetworkDelimeter(int network) {
        this.network = network;
    }

    public int getNetwork() {
        return network;
    }
}
