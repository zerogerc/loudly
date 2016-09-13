package ly.loud.loudly.base.exceptions;

import ly.loud.loudly.networks.Networks.Network;

/**
 * Error that's risen when we done too many requests per second
 */
public class TooManyRequestsException extends NetworkException {
    public TooManyRequestsException(@Network int network) {
        super(network);
    }
}
