package ly.loud.loudly.networks.facebook.entities;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import ly.loud.loudly.base.exceptions.NetworkException;
import ly.loud.loudly.base.exceptions.TokenExpiredException;
import ly.loud.loudly.base.exceptions.TooManyRequestsException;

import static ly.loud.loudly.networks.Networks.FB;

public class Error {
    public String message;

    public int code;

    public String type;

    /**
     * @see <a href=https://developers.facebook.com/docs/graph-api/using-graph-api>Facebook Api</a>
     */
    @NonNull
    public IOException toException() {
        Log.e("FACEBOOK API", message);
        if (type.equals("OAuthException") || code == 463 || code == 464 || code == 467) {
            return new TokenExpiredException(FB);
        }
        switch (code) {
            case 4:
            case 17:
            case 341:
                // Too many requests
                return new TooManyRequestsException(FB);
            default:
                return new NetworkException(FB);
        }
    }
}
