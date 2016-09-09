package ly.loud.loudly.networks.instagram.entities;

import com.google.gson.annotations.SerializedName;

public class InstagramComment {
    @SerializedName("created_time")
    public long createdTime;

    public String text;

    public InstagramPerson from;

    public String id;

}
