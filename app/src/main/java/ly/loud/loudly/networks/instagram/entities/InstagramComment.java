package ly.loud.loudly.networks.instagram.entities;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import ly.loud.loudly.base.single.Comment;

import static ly.loud.loudly.base.entities.Info.emptyInfo;
import static ly.loud.loudly.networks.Networks.INSTAGRAM;
import static ly.loud.loudly.util.ListUtils.emptyArrayList;

public class InstagramComment {
    @SerializedName("created_time")
    public long createdTime;

    public String text;

    public InstagramPerson from;

    public String id;

    @NonNull
    public Comment toComment() {
        return new Comment(
                text,
                createdTime,
                emptyArrayList(),
                from.toPerson(),
                INSTAGRAM,
                id,
                emptyInfo()
        );
    }
}
