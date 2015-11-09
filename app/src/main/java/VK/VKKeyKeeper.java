package VK;

import android.os.Parcel;

import base.KeyKeeper;

public class VKKeyKeeper extends KeyKeeper {
    public static final String CLIENT_ID = "5133011";
    private String accessToken = null;
    private int userID = 0;

    public VKKeyKeeper() {}

    public VKKeyKeeper(String accessToken, int userID) {
        this.accessToken = accessToken;
        this.userID = userID;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getUserID() {
        return userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeInt(userID);
    }
    public static final Creator<VKKeyKeeper> CREATOR = new Creator<VKKeyKeeper>() {
        @Override
        public VKKeyKeeper createFromParcel(Parcel source) {
            String token = source.readString();
            int ID = source.readInt();
            return new VKKeyKeeper(token, ID);
        }

        @Override
        public VKKeyKeeper[] newArray(int size) {
            return new VKKeyKeeper[size];
        }
    };
}
