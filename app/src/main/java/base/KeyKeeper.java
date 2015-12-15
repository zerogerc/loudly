package base;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Store keys, that we need in order to interact with social network
 */
public abstract class KeyKeeper implements Parcelable {
    protected long validThrough;

    private static final char SEPARATOR = '&';
    public KeyKeeper() {}

    /**
     * @return content of KeyKeeper as list of strings
     */
    protected abstract String[] toStrings();
    protected abstract void fromStrings(String[] strings);

    public void expiresIn(long time) {
        validThrough = Calendar.getInstance().getTimeInMillis() / 1000 + time - 60 * 5;
    }

    /**
     * May the token expire in hour?
     */
    public boolean mayExpire() {
        return validThrough - Calendar.getInstance().getTimeInMillis() / 1000 < 60 * 60;
    }

    public boolean isValid() {
        return Calendar.getInstance().getTimeInMillis() / 1000 < validThrough;
    }

    public String toStringBundle() {
        String[] strings = toStrings();
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
            sb.append(SEPARATOR);
        }
        return sb.toString();
    }

    public static KeyKeeper fromStringBundle(int network, String bundle) {
        String[] strings = bundle.split(String.valueOf(SEPARATOR));
        KeyKeeper result = Networks.makeKeyKeeper(network);
        result.fromStrings(strings);
        return result;
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
