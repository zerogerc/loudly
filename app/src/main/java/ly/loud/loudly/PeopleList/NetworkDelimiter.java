package ly.loud.loudly.PeopleList;

/**
 * Created by ZeRoGerc on 07.12.15.
 */
public class NetworkDelimiter implements Item {
    private int network;

    public NetworkDelimiter() {
        this.network = -1;
    }

    public NetworkDelimiter(int network) {
        this.network = network;
    }

    public int getNetwork() {
        return network;
    }
}
