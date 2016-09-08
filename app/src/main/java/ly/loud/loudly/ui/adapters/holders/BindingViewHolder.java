package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ly.loud.loudly.ui.adapters.holders.ListItem;

public abstract class BindingViewHolder<T extends ListItem> extends RecyclerView.ViewHolder {

    public abstract void bind(@NonNull T item);

    public BindingViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
