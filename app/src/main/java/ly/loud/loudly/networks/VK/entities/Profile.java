package ly.loud.loudly.networks.VK.entities;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Danil Kolikov
 */
public class Profile {
    public long id;
    @SerializedName("first_name")
    public String firstName;
    @SerializedName("last_name")
    public String lastName;
    @SerializedName("photo_50")
    public String photo50;
}
