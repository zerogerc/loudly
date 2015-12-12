package base;

import base.says.Info;

/**
 * Interface for objects which are in some network
 */
public interface SingleNetwork {
    void setNetwork(int network);
    int getNetwork();

    boolean existsIn(int network);

    String getId();
    void setId(String id);

    Info getInfo();
    void setInfo(Info info);
}
