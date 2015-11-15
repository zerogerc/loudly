package MailRu;

import android.os.Parcel;

import java.io.IOException;

import base.KeyKeeper;
import util.FileWrap;

public class MailRuKeyKeeper extends KeyKeeper {
    public static final String CLIENT_ID = "738872";
    private String sessionKey;
    private String refreshToken;

    public MailRuKeyKeeper() {}

    public MailRuKeyKeeper(String sessionKey, String refreshToken) {
        this.sessionKey = sessionKey;
        this.refreshToken = refreshToken;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    protected String[] toStrings() {
        return new String[]{sessionKey, refreshToken};
    }

    @Override
    public void readFromFile(FileWrap file) throws IOException {
        sessionKey = file.readString();
        refreshToken = file.readString();
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
