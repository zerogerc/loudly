package Facebook;

import android.os.Parcel;

import base.KeyKeeper;

public class FacebookKeyKeeper extends KeyKeeper {
    public static final String CLIENT_ID = "443913362466352";
    private String accessToken = null;

    public FacebookKeyKeeper() {
        super();
    }

    public FacebookKeyKeeper(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    protected String[] toStrings() {
        return new String[]{accessToken, Long.toString(validThrough)};
    }

    @Override
    protected void fromStrings(String[] strings) {
        accessToken = strings[0];
        validThrough = Long.parseLong(strings[1]);
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