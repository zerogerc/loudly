package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import ly.loud.loudly.base.exceptions.NetworkException;
import ly.loud.loudly.base.exceptions.TokenExpiredException;
import ly.loud.loudly.base.exceptions.TooManyRequestsException;

import static ly.loud.loudly.networks.Networks.INSTAGRAM;

/**
 * Object with error and it's description
 */
public class Meta {
    @SerializedName("error_type")
    public String errorType;

    public int code;

    @SerializedName("error_message")
    public String errorMessage;

    @NonNull
    public IOException getException() {
        Log.e("INSTAGRAM API", errorMessage);
        if (errorType.equals("OAuthAccessTokenException")) {
            return new TokenExpiredException(INSTAGRAM);
        }
        if (errorType.equals("OAuthRateLimitException")) {
            return new TooManyRequestsException(INSTAGRAM);
        }
        return new NetworkException(INSTAGRAM);
    }
}
