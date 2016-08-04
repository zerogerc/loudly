package ly.loud.loudly.ui.brand_new.adapter;

import ly.loud.loudly.ui.brand_new.ItemTypeAnnotation.ItemType;

public interface ListItem {
    @ItemType
    int getType();
}
