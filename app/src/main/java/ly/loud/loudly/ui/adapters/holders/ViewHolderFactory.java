package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import static ly.loud.loudly.ui.adapters.holders.ItemTypes.COMMENT;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.DELIMITER;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.PERSON;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.POST;

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
            case COMMENT:
                return  new ViewHolderComment(inflater, parent);
            case DELIMITER:
                return new ViewHolderDelimiter(inflater, parent);
            default: //LOAD_MORE
                return new ViewHolderLoadMore(inflater, parent);
        }
    }
}
