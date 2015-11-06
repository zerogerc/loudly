package VK;

import android.os.Parcel;

import base.KeyKeeper;

public class VKKeyKeeper extends KeyKeeper {
    private String accessToken = null;
    public static final String CLIENT_ID = "5133011";;

    public VKKeyKeeper() {}

    public VKKeyKeeper(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
    }
    public static final Creator<VKKeyKeeper> CREATOR = new Creator<VKKeyKeeper>() {
        @Override
        public VKKeyKeeper createFromParcel(Parcel source) {
            String token = source.readString();
            return new VKKeyKeeper(token);
        }

        @Override
        public VKKeyKeeper[] newArray(int size) {
            return new VKKeyKeeper[size];
        }
    };
}
