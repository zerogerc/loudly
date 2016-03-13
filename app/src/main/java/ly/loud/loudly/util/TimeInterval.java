package ly.loud.loudly.util;

public class TimeInterval {
    public long from, to;

    public TimeInterval(long from, long to) {
        this.from = from;
        this.to = to;
    }
    public TimeInterval copy() {
        return new TimeInterval(from, to);
    }

    public static TimeInterval any() {
        return new TimeInterval(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static TimeInterval since(long time) {
        return new TimeInterval(time, Long.MAX_VALUE);
    }

    public static TimeInterval before(long time) {
        return new TimeInterval(Long.MIN_VALUE, time);
    }

    public boolean contains(long x) {
        if (to == -1) {
            return from < x;
        }
        if (from == -1) {
            return x < to;
        }
        return from < x && x < to;
    }
}
