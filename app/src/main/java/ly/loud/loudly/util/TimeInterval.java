package ly.loud.loudly.util;

import android.support.annotation.NonNull;

public class TimeInterval {
    public long from, to;

    public TimeInterval(long from, long to) {
        this.from = from;
        this.to = to;
    }
    public TimeInterval copy() {
        return new TimeInterval(from, to);
    }

    @NonNull
    public static TimeInterval any() {
        return new TimeInterval(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @NonNull
    public static TimeInterval since(long time) {
        return new TimeInterval(time, Long.MAX_VALUE);
    }

    @NonNull
    public static TimeInterval before(long time) {
        return new TimeInterval(Long.MIN_VALUE, time);
    }

    public boolean contains(long x) {
        if (to == Long.MIN_VALUE) {
            return from < x;
        }
        if (from == Long.MAX_VALUE) {
            return x < to;
        }
        return from < x && x < to;
    }
}
