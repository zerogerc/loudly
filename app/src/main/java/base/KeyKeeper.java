package base;

import android.os.Parcel;
import android.os.Parcelable;

import Facebook.FacebookKeyKeeper;
import MailRu.MailRuKeyKeeper;
import VK.VKKeyKeeper;
import util.FileWrap;
import util.Writable;

/**
 * Store keys, that we need in order to interact with social network
 */
public abstract class KeyKeeper implements Parcelable, Writable {
    public KeyKeeper() {}

    /**
     * @return content of KeyKeeper as list of strings
     */
    protected abstract String[] toStrings();

    @Override
    public void writeToFile(FileWrap file) {
        String[] strings = toStrings();
        for (String s : strings) {
            file.writeString(s);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String[] strings = toStrings();
        for (String s : strings) {
            dest.writeString(s);
        }
    }

    /**
     * Make proper instance of KeyKeeper for the network
     * @param network ID of the network
     * @return KeyKeeper for the network
     */
    public static KeyKeeper makeKeyKeeper(int network) {
        switch (network) {
            case Networks.FB:
                return new FacebookKeyKeeper();
            case Networks.VK:
                return new VKKeyKeeper();
            case Networks.MAILRU:
                return new MailRuKeyKeeper();
            default:
                return null;
        }
    }
}
