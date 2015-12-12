package base;

import base.says.Info;

/**
 * Interface for objects which are in many networks
 */

public interface MultipleNetwork extends SingleNetwork {
    String getId(int network);
    void setId(int network, String link);
    String[] getIds();

    Info getInfo(int network);
    void setInfo(int network, Info info);
}
