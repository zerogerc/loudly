package ly.loud.loudly.ui.brand_new.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import static ly.loud.loudly.ui.brand_new.ItemTypeAnnotation.ItemType;
import static ly.loud.loudly.ui.brand_new.ItemTypeAnnotation.PERSON;

public class ViewHolderFactory {
    public static BindingViewHolder provideViewHolder(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent,
            @ItemType int type
    ) {
        switch (type) {
            case PERSON:
                return new ViewHolderPerson(inflater, parent);
            default:
                return new ViewHolderDelimiter(inflater, parent);
        }
    }
}
