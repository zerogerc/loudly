package ly.loud.loudly.ui.new_post;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.networks.Networks;
import solid.collections.SolidList;

public class NetworkChooseAdapter extends RecyclerView.Adapter<NetworkChooseAdapter.NetworkChooseHolder> {

    @NonNull
    private final Context context;

    @NonNull
    private SolidList<NetworkContract> models;

    private boolean[] selected = new boolean[Networks.NETWORK_COUNT];

    public NetworkChooseAdapter(@NonNull Context context) {
        this.context = context;
        this.models = SolidList.empty();
        Arrays.fill(selected, true);
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

    @NonNull
    public SolidList<NetworkContract> getModels() {
        return models;
    }

    public void setModels(@NonNull SolidList<NetworkContract> models) {
        this.models = models;
        notifyDataSetChanged();
    }

    @NonNull
    public boolean[] getSelectedNetworks() {
        return selected;
    }

    public void setSelectedNetworks(@NonNull boolean[] selected) {
        this.selected = selected;
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

        /**
         * Id of currently bound network.
         */
        private int currentId = 0;

        public NetworkChooseHolder(@NonNull Context context, @NonNull ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.network_choose_list_item, parent, false));
            ButterKnife.bind(this, itemView);
        }

        public void refresh(@NonNull NetworkContract model) {
            currentId = model.getId();
            textView.setText(model.getFullName());
            checkBox.setChecked(selected[currentId]);
        }

        @OnClick(R.id.network_choose_list_item_root)
        public void onClick() {
            checkBox.setChecked(!checkBox.isChecked());
            selected[currentId] = checkBox.isChecked();
        }
    }
}
