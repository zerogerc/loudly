package ly.loud.loudly.networks.vk;

import android.os.Parcel;
import android.support.annotation.NonNull;

import ly.loud.loudly.networks.KeyKeeper;

public class VKKeyKeeper extends KeyKeeper {
    @NonNull
    private final String accessToken;

    @NonNull
    private final String userId;

    public VKKeyKeeper(@NonNull String accessToken, @NonNull String userID) {
        this.accessToken = accessToken;
        this.userId = userID;
    }

    public VKKeyKeeper(@NonNull String[] stored) {
        accessToken = stored[0];
        userId = stored[1];
    }

    @NonNull
    public String getAccessToken() {
        return accessToken;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @Override
    protected String[] toStrings() {
        return new String[] {accessToken, userId};
    }

    public static final Creator<VKKeyKeeper> CREATOR = new Creator<VKKeyKeeper>() {
        @Override
        public VKKeyKeeper createFromParcel(Parcel source) {
            String token = source.readString();
            String ID = source.readString();
            return new VKKeyKeeper(token, ID);
        }

        @Override
        public VKKeyKeeper[] newArray(int size) {
            return new VKKeyKeeper[size];
        }
    };
}
