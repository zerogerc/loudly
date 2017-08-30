package ly.loud.loudly.networks.facebook.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.util.ListUtils;

import static ly.loud.loudly.networks.Networks.FB;

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

    @NonNull
    private ArrayList<SingleAttachment> getAttachments() {
        return attachment == null ? ListUtils.emptyArrayList() :
                ListUtils.asArrayList(attachment.toAttachment());
    }

    @NonNull
    public Comment toComment(@NonNull FbPerson person) {
        return new Comment(message, createdTime, getAttachments(), person.toPerson(),
                FB, id, new Info(likeCount, 0, commentCount));
    }

}
