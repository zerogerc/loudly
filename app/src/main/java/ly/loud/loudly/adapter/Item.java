package ly.loud.loudly.adapter;

/**
 * Created by ZeRoGerc on 07.12.15.
 */
public interface Item {
    int DELIMITER = 1;
    int PERSON = 2;
    int COMMENT = 3;
    int POST = 4;

    int getType();
}

