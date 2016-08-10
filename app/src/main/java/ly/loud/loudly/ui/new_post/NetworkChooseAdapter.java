package ly.loud.loudly.ui.new_post;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.networks.NetworkContract;

public class NetworkChooseAdapter extends RecyclerView.Adapter<NetworkChooseAdapter.NetworkChooseHolder> {

    @NonNull
    private final Context context;

    @NonNull
    private final List<NetworkContract> models;

    @Nullable
    private OnItemStateChangeListener onItemStateChangeListener;

    public NetworkChooseAdapter(@NonNull Context context, @NonNull List<NetworkContract> models) {
        this.context = context;
        this.models = models;
    }

    @Override
    public NetworkChooseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NetworkChooseHolder(context, parent);
    }

    @Override
    public void onBindViewHolder(NetworkChooseHolder holder, int position) {
        holder.refresh(models.get(position));
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public void setOnItemStateChangeListener(@NonNull OnItemStateChangeListener onItemStateChangeListener) {
        this.onItemStateChangeListener = onItemStateChangeListener;
    }

    public class NetworkChooseHolder extends RecyclerView.ViewHolder {

        @SuppressWarnings("NullableProblems") // Butterknife
        @BindView(R.id.network_choose_list_item_check)
        @NonNull
        CheckBox checkBox;

        @SuppressWarnings("NullableProblems") // Butterknife
        @BindView(R.id.network_choose_list_item_text)
        @NonNull
        TextView textView;

        public NetworkChooseHolder(@NonNull Context context, @NonNull ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.network_choose_list_item, parent, false));
            ButterKnife.bind(this, itemView);
        }

        public void refresh(@NonNull NetworkContract models) {
            textView.setText(models.getFullName());
        }

        @OnClick(R.id.network_choose_list_item_root)
        public void onClick() {
            checkBox.setChecked(!checkBox.isChecked());
            if (onItemStateChangeListener != null) {
                onItemStateChangeListener.onItemStateChange(getAdapterPosition(), checkBox.isChecked());
            }
        }
    }

    public interface OnItemStateChangeListener {
        /**
         * @param index - index of changed item in adapter
         * @param state - <code>true</code> if switch is on
         */
        void onItemStateChange(int index, boolean state);
    }
}
