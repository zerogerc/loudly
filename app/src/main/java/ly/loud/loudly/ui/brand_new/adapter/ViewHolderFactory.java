package ly.loud.loudly.ui.brand_new.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import static ly.loud.loudly.ui.brand_new.ItemTypes.ItemType;
import static ly.loud.loudly.ui.brand_new.ItemTypes.PERSON;
import static ly.loud.loudly.ui.brand_new.ItemTypes.POST;

public class ViewHolderFactory {
    public static BindingViewHolder provideViewHolder(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent,
            @ItemType int type
    ) {
        switch (type) {
            case POST:
                return new ViewHolderPerson(inflater, parent);
            case PERSON:
                return new ViewHolderPerson(inflater, parent);
            default:
                return new ViewHolderDelimiter(inflater, parent);
        }
    }
}
