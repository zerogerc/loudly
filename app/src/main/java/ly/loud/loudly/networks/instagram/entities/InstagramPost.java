package ly.loud.loudly.networks.instagram.entities;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;

import static ly.loud.loudly.networks.Networks.INSTAGRAM;
import static ly.loud.loudly.util.ListUtils.asArrayList;
import static ly.loud.loudly.util.ListUtils.emptyArrayList;

public class InstagramPost {
    public Caption caption;

    @SerializedName("created_time")
    public long createdTime;

    public String id;

    public Counter comments, likes;

    @Nullable
    public Images images;

    @NonNull
    public SinglePost toPost() {
        ArrayList<SingleAttachment> attachments;
        if (images != null) {
            Image instagramImage = images.standardResolution;
            SingleImage image = new SingleImage(
                    instagramImage.url,
                    new Point(instagramImage.width, instagramImage.height),
                    INSTAGRAM,
                    instagramImage.url
            );
            attachments = asArrayList(image);
        } else {
            attachments = emptyArrayList();
        }
        return new SinglePost(
                caption.text,
                createdTime,
                attachments,
                null,
                INSTAGRAM,
                id,
                new Info(likes.count, 0, comments.count)
        );
    }
}
