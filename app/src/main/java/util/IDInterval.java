package util;

public class IDInterval extends Interval<String> {
    public IDInterval(String from, String to) {
        super(from, to);
    }

    public IDInterval copy() {
        return new IDInterval(from, to);
    }

    public static IDInterval any() {
        return new IDInterval("", "");
    }

    public static IDInterval since(String id) {
        return new IDInterval(id, "");
    }

    public static IDInterval before(String id) {
        return new IDInterval("", id);
    }

    @Override
    public boolean contains(String x) {
        if (from.isEmpty() && to.isEmpty()) {
            return true;
        }
        if (from.isEmpty()) {
            return x.compareTo(to) < 0;
        }
        if (to.isEmpty()) {
            return from.compareTo(x) < 0;
        }
        return from.compareTo(x) < 0 && x.compareTo(to) < 0;
    }
}
