package ly.loud.loudly.networks.loudly;

import android.os.Parcel;

import ly.loud.loudly.networks.KeyKeeper;

public class LoudlyKeyKeeper extends KeyKeeper {
    public LoudlyKeyKeeper() {
        super();
    }

    public LoudlyKeyKeeper(String[] strings){

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
