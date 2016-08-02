package ly.loud.loudly.new_base;

import java.io.IOException;

/**
 * Exception representing situation when user's token expired
 *
 * @author Danil Kolikov
 */
public class TokenExpiredException extends IOException {
    public TokenExpiredException() {
        super();
    }
}
