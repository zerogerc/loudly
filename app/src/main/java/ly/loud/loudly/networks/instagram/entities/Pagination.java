package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Link to next page of results
 */
public class Pagination {
    @SerializedName("next_url")
    @Nullable
    public String nextUrl;
}
