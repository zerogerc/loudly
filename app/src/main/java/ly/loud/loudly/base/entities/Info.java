package ly.loud.loudly.base.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

/**
 * Information about likes, shares and comments in social network
 */
public class Info implements Parcelable {
    private static final Info EMPTY_INFO = new Info(0, 0, 0);

    public final int like;
    public final int repost;
    public final int comment;

    public Info(int like, int repost, int comment) {
        this.like = like;
        this.repost = repost;
        this.comment = comment;
    }

    public Info() {
        like = 0;
        repost = 0;
        comment = 0;
    }

    public Info(Parcel source) {
        this.like = source.readInt();
        this.repost = source.readInt();
        this.comment = source.readInt();
    }

    @NonNull
    public static Info emptyInfo() {
        return EMPTY_INFO;
    }

    @CheckResult
    @NonNull
    public Info add(@NonNull Info info) {
        return new Info(like + info.like, repost + info.repost, comment + info.comment);
    }

    @CheckResult
    @NonNull
    public Info subtract(@NonNull Info info) {
        return new Info(like - info.like, repost - info.repost, comment - info.comment);
    }

    public boolean isEmpty() {
        return like == 0 && repost == 0 && comment == 0;
    }

    public boolean hasPositiveChanges() {
        return like > 0 || repost > 0 || comment > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Info)) {
            return false;
        }
        Info second = (Info) o;
        return like == second.like && repost == second.repost && comment == second.comment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(like);
        dest.writeInt(repost);
        dest.writeInt(comment);
    }

    public static final Creator<Info> CREATOR = new Creator<Info>() {
        @Override
        public Info createFromParcel(Parcel source) {
            return new Info(source);
        }

        @Override
        public Info[] newArray(int size) {
            return new Info[size];
        }
    };
}
