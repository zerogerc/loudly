package ly.loud.loudly.base.exceptions;

/**
 * Error that's risen when we done too many requests per second
 */
public class TooManyRequestsException extends NetworkException {
    public TooManyRequestsException(int network) {
        super(network);
    }
}
