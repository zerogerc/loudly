package base;

import base.says.Info;

/**
 * Interface for objects which are in many networks. They are stored in the local database
 */

public interface MultipleNetwork extends SingleNetwork {
    Link getId(int network);
    void setId(int network, Link id);
    Link[] getIds();

    Info getInfo(int network);
    void setInfo(int network, Info info);
}
