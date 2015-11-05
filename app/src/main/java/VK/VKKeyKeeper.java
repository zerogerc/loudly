package VK;

import base.KeyKeeper;

/**
 * Created by ZeRoGerc on 05.11.15.
 */
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
}
