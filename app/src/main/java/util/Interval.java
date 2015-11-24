package util;

public abstract class Interval<T extends Comparable<T>> {
    public T from, to;

    public Interval(T from, T to) {
        this.from = from;
        this.to = to;
    }

    public abstract boolean contains(T x);
}
