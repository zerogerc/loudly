package ly.loud.loudly.networks.vk.entities;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Post / comment in VK api
 *
 * @author Danil Kolikov
 */
public class Say {
    public String id;

    @SerializedName("from_id")
    public String fromId;

    public long date;

    @Nullable
    public String text;

    public Counter likes, reposts, comments;

    public List<Attachment> attachments;
}
