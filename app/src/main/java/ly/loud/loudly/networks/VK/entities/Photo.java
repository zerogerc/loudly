package ly.loud.loudly.networks.VK.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Photo for VK api
 *
 * @author Danil Kolikov
 */
public class Photo {
    public String id;
    @SerializedName("photo_604")
    public String photo604;
    public int width, height;
}
