package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class InstagramPost {
    public Caption caption;

    @SerializedName("created_time")
    public long createdTime;

    public String id;

    public Counter comments, likes;

    @Nullable
    public Images images;
}
