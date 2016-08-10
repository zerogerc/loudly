package ly.loud.loudly.networks.facebook.entities;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Danil Kolikov
 */
public class Post {
    @Nullable
    public String message;

    @SerializedName("created_time")
    public long createdTime;

    public String id;

    @Nullable
    public Shares shares;

    @Nullable
    public Summary likes, comments;

    @Nullable
    public Data<List<FbAttachment>> attachments;
}
