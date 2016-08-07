package ly.loud.loudly.networks.mail_ru;

import android.os.Parcel;
import android.support.annotation.NonNull;

import ly.loud.loudly.networks.KeyKeeper;

public class MailRuKeyKeeper extends KeyKeeper {
    public static final String CLIENT_ID = "738872";
    @NonNull
    private final String sessionKey;

    @NonNull
    private final String refreshToken;

    public MailRuKeyKeeper(@NonNull String sessionKey, @NonNull String refreshToken) {
        this.sessionKey = sessionKey;
        this.refreshToken = refreshToken;
    }

    public MailRuKeyKeeper(@NonNull String[] stored) {
        sessionKey = stored[0];
        refreshToken = stored[1];
    }

    @NonNull
    public String getSessionKey() {
        return sessionKey;
    }

    @NonNull
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    protected String[] toStrings() {
        return new String[]{sessionKey, refreshToken};
    }

    public static final Creator<MailRuKeyKeeper> CREATOR = new Creator<MailRuKeyKeeper>() {
        @Override
        public MailRuKeyKeeper createFromParcel(Parcel source) {
            return new MailRuKeyKeeper(source.readString(), source.readString());
        }

        @Override
        public MailRuKeyKeeper[] newArray(int size) {
            return new MailRuKeyKeeper[size];
        }
    };
}
