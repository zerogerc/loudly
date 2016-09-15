package ly.loud.loudly.networks.ok;

import android.os.Parcel;
import android.support.annotation.NonNull;

import ly.loud.loudly.networks.KeyKeeper;

public class OkKeyKeeper extends KeyKeeper {
    @NonNull
    private final String accessToken;

    @NonNull
    private final String sessionKey;

    public OkKeyKeeper(@NonNull String accessToken, @NonNull String sessionKey) {
        this.accessToken = accessToken;
        this.sessionKey = sessionKey;
    }

    public OkKeyKeeper(@NonNull String[] strings) {
        this(strings[0], strings[1]);
    }

    @Override
    protected String[] toStrings() {
        return new String[]{accessToken, sessionKey};
    }

    @NonNull
    public String getAccessToken() {
        return accessToken;
    }

    @NonNull
    public String getSessionKey() {
        return sessionKey;
    }

    public static final Creator<OkKeyKeeper> CREATOR = new Creator<OkKeyKeeper>() {
        @Override
        public OkKeyKeeper createFromParcel(Parcel source) {
            return new OkKeyKeeper(source.readString(), source.readString());
        }

        @Override
        public OkKeyKeeper[] newArray(int size) {
            return new OkKeyKeeper[size];
        }
    };
}
