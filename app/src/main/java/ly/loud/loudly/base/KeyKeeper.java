package ly.loud.loudly.base;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

/**
 * Store keys, that we need in order to interact with social network
 */
public abstract class KeyKeeper implements Parcelable {
    private static final char SEPARATOR = '&';
    private volatile int instances;

    public KeyKeeper() {
    }

    public static KeyKeeper fromStringBundle(int network, String bundle) {
        String[] strings = bundle.split(String.valueOf(SEPARATOR));
        KeyKeeper result = Networks.makeKeyKeeper(network);
        result.fromStrings(strings);
        return result;
    }

    /**
     * @return content of KeyKeeper as list of strings
     */
    protected abstract String[] toStrings();

    protected abstract void fromStrings(String[] strings);

    // ToDo: Maybe it's possible to simplify this?
    public <T> T doWithKeys(Action<T> action) throws IOException {
        instances++;
        T result = action.execute(this);
        instances--;
        return result;
    }

    public boolean isBusy() {
        return instances != 0;
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

    public static abstract class Action<T> {
        public abstract T execute(KeyKeeper keyKeeper) throws IOException;
    }

}
