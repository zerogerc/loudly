package ly.loud.loudly.networks.Facebook.entities;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

/**
 * @author Danil Kolikov
 */
public class FbComment {
    public String id;

    public FbPerson from;

    @Nullable
    public String message;

    @SerializedName("created_time")
    public long createdTime;

    @SerializedName("comment_count")
    public int commentCount;

    @SerializedName("like_count")
    public int likeCount;

    @Nullable
    public FbAttachment attachment;
}
