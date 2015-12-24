package ly.loud.loudly.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ZeRoGerc on 25.12.15.
 */
public abstract class ViewHolder extends RecyclerView.ViewHolder {
    private Activity activity;

    public ViewHolder(Activity activity, View itemView) {
        super(itemView);
    }

    public abstract void refresh(Item item);

    public static ViewHolder makeViewHolder(Activity activity, ViewGroup parent, int viewType) {
        switch (viewType) {
            case Item.COMMENT:
                return new ViewHolderComment(activity, parent);
            case Item.PERSON:
                return new ViewHolderPerson(activity, parent);
            default:
                return new ViewHolderDelimiter(activity, parent);
        }
    }

    public Activity getActivity() {
        return activity;
    }
}
