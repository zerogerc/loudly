package ly.loud.loudly.base.exceptions;

import java.io.IOException;

import ly.loud.loudly.networks.Networks.Network;

/**
 * Exception of some network
 */
public class NetworkException extends IOException {
    @Network
    public final int network;

    public NetworkException(@Network int network) {
        this.network = network;
    }
}
