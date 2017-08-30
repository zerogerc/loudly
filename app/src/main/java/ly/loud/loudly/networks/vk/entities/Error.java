package ly.loud.loudly.networks.vk.entities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import ly.loud.loudly.base.exceptions.FatalNetworkException;
import ly.loud.loudly.base.exceptions.NetworkException;
import ly.loud.loudly.base.exceptions.TokenExpiredException;
import ly.loud.loudly.base.exceptions.TooManyRequestsException;

import static ly.loud.loudly.networks.Networks.VK;

/**
 * Error for VK api
 *
 * @author Danil Kolikov
 */
public class Error {
    @SerializedName("error_code")
    public int errorCode;

    @SerializedName("error_msg")
    public String errorMessage;

    /**
     * Transform VK Api error to Exception
     *
     * @return Exception
     * @see <a href="https://vk.com/dev/errors">VK Api</a>
     */
    @NonNull
    public IOException toException() {
        Log.e("VK API", errorMessage);
        switch (errorCode) {
            case 1:
                // Unknown exception
                return new FatalNetworkException(VK);
            case 6:
            case 9:
                // Too many requests
                return new TooManyRequestsException(VK);
            case 5:
                // Token expired
                return new TokenExpiredException(VK);
            default:
                // Other
                return new NetworkException(VK);
        }
    }
}
