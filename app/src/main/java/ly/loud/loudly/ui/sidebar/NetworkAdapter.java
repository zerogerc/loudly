package ly.loud.loudly.ui.sidebar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ly.loud.loudly.R;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.NetworkViewHolder> {

    @Override
    public NetworkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(NetworkViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private static class NetworkViewHolder extends RecyclerView.ViewHolder {

        public NetworkViewHolder(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup parent
        ) {
            super(inflater.inflate(R.layout.sidebar_network_item, parent, false));
        }
    }
}
