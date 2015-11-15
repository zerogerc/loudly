package VK;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import base.KeyKeeper;
import util.FileWrap;
import util.Writable;

public class VKKeyKeeper extends KeyKeeper {
    public static final String CLIENT_ID = "5133011";
    private String accessToken = null;
    private String userId = null;

    public VKKeyKeeper() {}

    public VKKeyKeeper(String accessToken, String userID) {
        this.accessToken = accessToken;
        this.userId = userID;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    protected String[] toStrings() {
        return new String[] {accessToken, userId};
    }

    @Override
    public void readFromFile(FileWrap file) throws IOException {
        accessToken = file.readString();
        userId = file.readString();
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
