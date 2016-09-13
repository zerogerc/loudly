package ly.loud.loudly.base.exceptions;

/**
 * Exception that rises when there is no internet connection
 */
public class NoNetworkConnectionException extends FatalException {
    public NoNetworkConnectionException() {
        super();
    }

    public NoNetworkConnectionException(Throwable cause) {
        super(cause);
    }
}
