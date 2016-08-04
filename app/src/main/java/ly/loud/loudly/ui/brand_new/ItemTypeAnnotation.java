package ly.loud.loudly.ui.brand_new;

import android.support.annotation.IntDef;

public abstract class ItemTypeAnnotation {
    @IntDef
    public @interface ItemType {}
    public static final int POST = 0;
    public static final int PERSON = 1;
    public static final int COMMENT = 2;
    public static final int DELIMITER = 3;

}
