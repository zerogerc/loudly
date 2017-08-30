package ly.loud.loudly.ui.adapters.holders;

import ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;

public interface ListItem {
    @ItemType
    int getType();
}
