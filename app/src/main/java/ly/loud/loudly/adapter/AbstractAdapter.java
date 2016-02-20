package ly.loud.loudly.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

public class AbstractAdapter<A extends Activity, T extends Item> extends RecyclerView.Adapter<ViewHolder> {
    //TODO: working with scrolling different types of ViewHolders
    protected List<T> items;
    protected A activity;

    protected boolean created = false;

    public AbstractAdapter(List<T> items, A activity) {
        this.items = items;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.makeViewHolder(activity, parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!created && position == 0) {
            created = true;
            onFirstItemAppeared();
        }

        Log.e("BIND", Integer.toString(items.size()) + ' ' + Integer.toString(position));
        holder.refresh(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onFirstItemAppeared() {} //TODO don't know how it really should be
}
