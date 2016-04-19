package ly.loud.loudly.networks.Instagram;

import android.os.Parcel;
import ly.loud.loudly.base.KeyKeeper;

/**
 * @author Danil Kolikov
 */
public class InstagramKeyKeeper extends KeyKeeper {
    static final String CLIENT_ID = "25767d36bc624fe58215881ac0318ab3";
    private String accessToken;

    public InstagramKeyKeeper() {
        super();
    }

    private InstagramKeyKeeper(String s) {
        accessToken = s;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    protected String[] toStrings() {
        return new String[]{accessToken};
    }

    @Override
    protected void fromStrings(String[] strings) {
        accessToken = strings[0];
    }

    public final static Creator<InstagramKeyKeeper> CREATOR = new Creator<InstagramKeyKeeper>() {
        @Override
        public InstagramKeyKeeper createFromParcel(Parcel source) {
            return new InstagramKeyKeeper(source.readString());
        }

        @Override
        public InstagramKeyKeeper[] newArray(int size) {
            return new InstagramKeyKeeper[size];
        }
    };
}
