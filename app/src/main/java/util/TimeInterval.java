package util;

public class TimeInterval extends Interval<Long> {
    public TimeInterval(Long from, Long to) {
        super(from, to);
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

    @Override
    public boolean contains(Long x) {
        return from < x && x < to;
    }
}
