package ly.loud.loudly.networks.instagram.entities;

import com.google.gson.annotations.SerializedName;

public class InstagramPerson {
    public String username;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("profile_picture")
    public String profilePicture;
}
