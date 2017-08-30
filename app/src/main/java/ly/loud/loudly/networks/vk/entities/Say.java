package ly.loud.loudly.networks.vk.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.base.single.SinglePost;

import static ly.loud.loudly.networks.Networks.VK;

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

    public List<VKAttachment> attachments;

    @NonNull
    private ArrayList<SingleAttachment> getAttachments() {
        if (attachments == null) {
            return new ArrayList<>();
        }
        ArrayList<SingleAttachment> attachments = new ArrayList<>();
        for (VKAttachment attachment : this.attachments) {
            SingleAttachment filled = attachment.toAttachment();
            if (filled == null) {
                continue;
            }
            attachments.add(filled);
        }
        return attachments;
    }

    private int get(@Nullable Counter counter) {
        return counter == null ? 0 : counter.count;
    }

    @NonNull
    public Info getInfo() {
        return new Info(get(likes), get(reposts), get(comments));
    }

    @NonNull
    public SinglePost toPost() {
        return new SinglePost(text, date, getAttachments(), null, VK, id, getInfo());
    }

    @NonNull
    public Comment toComment(@NonNull Profile from) {
        return  new Comment(text, date, getAttachments(), from.toPerson(), VK, id, getInfo());
    }
}
