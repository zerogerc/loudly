package util;

import base.Networks;
import base.Wrap;

public class WrapHolder {
    static Wrap[] wraps = new Wrap[Networks.NETWORKCOUNT];

    public static void addWrap(int network, Wrap w) {
        wraps[network] = w;
    }
    public static Wrap getWrap(int network) {
        return wraps[network];
    }
}
