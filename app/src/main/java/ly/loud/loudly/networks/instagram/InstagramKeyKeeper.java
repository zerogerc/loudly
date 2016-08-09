package ly.loud.loudly.networks.instagram;

import android.os.Parcel;
import android.support.annotation.NonNull;

import ly.loud.loudly.networks.KeyKeeper;

/**
 * @author Danil Kolikov
 */
public class InstagramKeyKeeper extends KeyKeeper {
    @NonNull
    private final String accessToken;

    public InstagramKeyKeeper(@NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    public InstagramKeyKeeper(@NonNull String[] stored) {
        accessToken = stored[0];
    }

    @NonNull
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    protected String[] toStrings() {
        return new String[]{accessToken};
    }

    public final static Creator<InstagramKeyKeeper> CREATOR = new Creator<InstagramKeyKeeper>() {
        @Override
        public InstagramKeyKeeper createFromParcel(Parcel source) {
            return new InstagramKeyKeeper(source.readString());
        }

        @Override
        public InstagramKeyKeeper[] newArray(int size) {
            return new InstagramKeyKeeper[size];
        }
    };
}
