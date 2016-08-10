package ly.loud.loudly.networks.facebook;

import android.os.Parcel;
import android.support.annotation.NonNull;

import ly.loud.loudly.networks.KeyKeeper;

public class FacebookKeyKeeper extends KeyKeeper {

    @NonNull
    private final String accessToken;

    public FacebookKeyKeeper(@NonNull String accessToken) {
        this.accessToken = accessToken;
    }

    public FacebookKeyKeeper(@NonNull String[] stored) {
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

    public static final Creator<FacebookKeyKeeper> CREATOR = new Creator<FacebookKeyKeeper>() {
        @Override
        public FacebookKeyKeeper createFromParcel(Parcel source) {
            return new FacebookKeyKeeper(source.readString());
        }

        @Override
        public FacebookKeyKeeper[] newArray(int size) {
            return new FacebookKeyKeeper[size];
        }
    };
}
