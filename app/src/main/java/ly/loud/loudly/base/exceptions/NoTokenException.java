package ly.loud.loudly.base.exceptions;

import ly.loud.loudly.networks.Networks;

/**
 * Exception that's risen when we try to do something with network api without token
 */
public class NoTokenException extends FatalNetworkException {
    public NoTokenException(@Networks.Network int network) {
        super(network);
    }
}
