package ly.loud.loudly.networks.vk.entities;

import android.support.annotation.Nullable;
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

    @SerializedName("width")
    public int widthPx;

    @SerializedName("height")
    public int heightPx;

    @Nullable
    public String text;

    public long date;
}
