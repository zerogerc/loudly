package ly.loud.loudly.ui.adapter;

/**
 * Items, BaseAdapter could handle
 *
 * Actions to implement new type of Item:
 *  1. Add new constant
 *  2. Inherit new class from Item
 *  3. getType() must return constant added above
 *  4. Implement ViewHolder for this new type
 */

public interface Item {
    int DELIMITER = 1;
    int PERSON = 2;
    int COMMENT = 3;
    int POST = 4;

    int getType();
}

