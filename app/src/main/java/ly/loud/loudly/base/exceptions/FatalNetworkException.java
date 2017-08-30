package ly.loud.loudly.base.exceptions;

import ly.loud.loudly.networks.Networks.Network;

public class FatalNetworkException extends FatalException {
    @Network
    public final int network;

    public FatalNetworkException(@Network int network) {
        this.network = network;
    }
}
