package ly.loud.loudly.networks;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Store keys, that we need in order to interact with social network
 */
public abstract class KeyKeeper implements Parcelable {
    private static final char SEPARATOR = '&';

    public KeyKeeper() {
    }

    @Nullable
    public static KeyKeeper fromStringBundle(int network, @NonNull String bundle) {
        String[] strings = bundle.split(String.valueOf(SEPARATOR));
        KeyKeeper result = Networks.makeKeyKeeper(network);
        if (result == null) {
            return null;
        }
        result.fromStrings(strings);
        return result;
    }

    /**
     * @return content of KeyKeeper as list of strings
     */
    protected abstract String[] toStrings();

    protected abstract void fromStrings(String[] strings);

    public String toStringBundle() {
        String[] strings = toStrings();
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
            sb.append(SEPARATOR);
        }
        return sb.toString();
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
}
