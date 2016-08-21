package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ly.loud.loudly.R;

public class ViewHolderLoadMore extends BindingViewHolder {

    public ViewHolderLoadMore(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_load_more, parent, false));
    }

    @Override
    public void bind(@Nullable ListItem item) {
        // nothing to do here
    }
}
