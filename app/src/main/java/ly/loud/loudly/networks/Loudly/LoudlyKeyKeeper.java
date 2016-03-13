package ly.loud.loudly.networks.Loudly;

import android.os.Parcel;

import ly.loud.loudly.base.KeyKeeper;

public class LoudlyKeyKeeper extends KeyKeeper {
    public LoudlyKeyKeeper() {
        super();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean mayExpire() {
        return false;
    }

    // Has nothing to save, later should say some keys
    @Override
    protected void fromStrings(String[] strings) {
    }

    @Override
    protected String[] toStrings() {
        return new String[]{""};
    }

    public static Creator<LoudlyKeyKeeper> CREATOR = new Creator<LoudlyKeyKeeper>() {
        @Override
        public LoudlyKeyKeeper createFromParcel(Parcel source) {
            return new LoudlyKeyKeeper();
        }

        @Override
        public LoudlyKeyKeeper[] newArray(int size) {
            return new LoudlyKeyKeeper[size];
        }
    };
}
