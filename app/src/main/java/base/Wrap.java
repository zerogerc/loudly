package base;

public abstract class Wrap<K extends KeyKeeper> {
    protected K keys;

    public K getKeys() {
        return keys;
    }

    public Wrap() {}

    public Wrap(K keys) {
        this.keys = keys;
    }
}
