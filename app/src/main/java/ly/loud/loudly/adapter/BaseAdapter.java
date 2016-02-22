package ly.loud.loudly.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by ZeRoGerc on 25.12.15.
 * ITMO University
 */


/**
 * Base class for adapters
 * No special actions
 * No animations
 *
 * @param <A> activity adapter is located on
 * @param <T> objects adapter is handling
 */
public class BaseAdapter<A extends Activity, T extends Item> extends RecyclerView.Adapter<ViewHolder> {
    protected List<T> items;
    protected A activity;

    public BaseAdapter(List<T> items, A activity) {
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
        holder.refresh(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
