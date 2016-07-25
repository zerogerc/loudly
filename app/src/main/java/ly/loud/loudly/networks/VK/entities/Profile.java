package ly.loud.loudly.networks.VK.entities;

import com.google.gson.annotations.SerializedName;

/**
 * User's profile in VK api
 *
 * @author Danil Kolikov
 */
public class Profile {
    public String id;
    @SerializedName("first_name")
    public String firstName;
    @SerializedName("last_name")
    public String lastName;
    @SerializedName("photo_50")
    public String photo50;
}
