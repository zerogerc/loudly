package ly.loud.loudly.networks.facebook.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.single.SinglePost;

import static ly.loud.loudly.networks.Networks.FB;

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

    @NonNull
    public Info getInfo() {
        int likes = this.likes != null ? this.likes.summary.totalCount : 0;
        int shares = this.shares != null ? this.shares.count : 0;
        int comments = this.comments != null ? this.comments.summary.totalCount : 0;
        return new Info(likes, shares, comments);
    }

    @NonNull
    private ArrayList<SingleAttachment> getAttachments() {
        ArrayList<SingleAttachment> attachments = new ArrayList<>();
        if (this.attachments != null) {
            //noinspection ConstantConditions It should have data
            for (FbAttachment attachment : this.attachments.data) {
                SingleAttachment parsed = attachment.toAttachment();
                if (parsed != null) {
                    attachments.add(parsed);
                }
            }
        }
        return attachments;
    }

    @NonNull
    public SinglePost toPost() {
        return new SinglePost(message, createdTime, getAttachments(), null, FB, id, getInfo());
    }
}
