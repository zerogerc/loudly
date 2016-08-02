package ly.loud.loudly.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import ly.loud.loudly.ui.adapter.person.ViewHolderPerson;

/**
 * Base class for ViewHolders BaseAdapter using
 *
 * Actions to implement new type of ViewHolder:
 *  1. Add case to makeViewHolder(), that return ViewHolder of new type
 *  2. Implement refresh()
 * @param <T> object ViewHolder handling
 */
public abstract class ViewHolder<T extends Item> extends RecyclerView.ViewHolder {
    private Activity activity;

    public ViewHolder(Activity activity, View itemView) {
        super(itemView);
        this.activity = activity;
    }

    /**
     * Method which refresh layout based on given item
     * @param item type of object to load on layout
     */
    public abstract void refresh(final T item);

    public static ViewHolder makeViewHolder(Activity activity, ViewGroup parent, int viewType) {
        switch (viewType) {
            case Item.COMMENT:
                return new ViewHolderComment(activity, parent);
            case Item.PERSON:
                return new ViewHolderPerson(activity, parent);
            case Item.POST:
                return new ViewHolderPost(activity, parent);
            default:
                return new ViewHolderDelimiter(activity, parent);
        }
    }

    public Activity getActivity() {
        return activity;
    }
}
