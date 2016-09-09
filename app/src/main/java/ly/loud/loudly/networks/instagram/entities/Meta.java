package ly.loud.loudly.networks.instagram.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Object with error and it's description
 */
public class Meta {
    @SerializedName("error_type")
    public String errorType;

    public int code;

    @SerializedName("error_message")
    public String errorMessage;
}
