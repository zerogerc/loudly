package ly.loud.loudly.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

public abstract class AbstractAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Item> items;
    private Activity activity;

    private boolean created = false;

    public AbstractAdapter(List<Item> items, Activity activity) {
        this.items = items;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.makeViewHolder(activity, parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!created && position == 0) {
            created = true;
            onFirstItemAppeared();
        }

        ((ViewHolder) holder).refresh(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public abstract void onFirstItemAppeared();
}
