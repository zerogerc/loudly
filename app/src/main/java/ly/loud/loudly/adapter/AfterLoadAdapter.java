package ly.loud.loudly.adapter;

import android.app.Activity;

import java.util.List;

/**
 * Adapter that do some actions when first item appearing
 * This action could be specified in method onFirstItemAppeared
 */
public abstract class AfterLoadAdapter extends BaseAdapter<Activity, Item> {
    protected boolean created = false;

    public AfterLoadAdapter(List<Item> items, Activity activity) {
        super(items, activity);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!created) {
            created = true;
            onFirstItemAppeared();
        }

        holder.refresh(items.get(position));
    }

    protected abstract void onFirstItemAppeared();
}
