package ly.loud.loudly.base.exceptions;

import java.io.IOException;

/**
 * Fatal exception which can't be handled automatically
 */
public class FatalException extends IOException {
    public FatalException() {
        super();
    }

    public FatalException(String detailMessage) {
        super(detailMessage);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }
}
