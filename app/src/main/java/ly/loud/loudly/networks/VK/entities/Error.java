package ly.loud.loudly.networks.VK.entities;

import com.google.gson.annotations.SerializedName;

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
}
