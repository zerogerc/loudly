package base;

import base.says.Info;

/**
 * Interface for objects which are in some network
 */
public interface SingleNetwork {
    void setNetwork(int network);
    int getNetwork();

    boolean exists();
    boolean existsIn(int network);

    Link getId();
    void setId(Link id);

    Info getInfo();
    void setInfo(Info info);
}
