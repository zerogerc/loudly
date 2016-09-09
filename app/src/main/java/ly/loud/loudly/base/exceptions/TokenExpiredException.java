package ly.loud.loudly.base.exceptions;

import ly.loud.loudly.networks.Networks.Network;

/**
 * Exception representing situation when user's token expired
 *
 * @author Danil Kolikov
 */
public class TokenExpiredException extends FatalException {
    @Network
    public final int network;

    public TokenExpiredException(@Network int network) {
        this.network = network;
    }
}
