package ly.loud.loudly.ui.sidebar;

import android.content.Context;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;
import ly.loud.loudly.R;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.sidebar.SideBarFragment.SideBarFragmentCallbacks;
import solid.collections.SolidList;

import static android.graphics.PorterDuff.Mode.*;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static solid.collections.SolidList.empty;

public class SideBarAdapter extends RecyclerView.Adapter<SideBarAdapter.SideBarViewHolder> {

    private static final int NETWORKS_OFFSET = 1;

    @NonNull
    private SolidList<NetworkContract> networkContracts = empty();

    @NonNull
    private final LayoutInflater inflater;

    @NonNull
    private final SideBarFragmentCallbacks callbacks;

    @State
    int selectedRow = NO_POSITION;

    public SideBarAdapter(@NonNull Context context, @NonNull SideBarFragmentCallbacks callbacks) {
        inflater = LayoutInflater.from(context);
        this.callbacks = callbacks;
    }

    @Override
    public SideBarViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        return new SideBarViewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SideBarViewHolder holder, int position) {
        if (position == 0) { // all posts
            holder.bind(R.drawable.icon_public_24dp, R.drawable.ic_chevron_right_24dp, R.string.everything);
        } else if (position == getItemCount() - 1) { // settings
            holder.bind(R.drawable.ic_settings_24dp, 0, R.string.title_activity_settings);
        } else {
            holder.bind(networkContracts.get(position - NETWORKS_OFFSET), selectedRow == position);
        }
    }

    private void switchToPosition(int newPosition) {
        int oldPosition = selectedRow;
        selectedRow = newPosition;

        if (oldPosition != NO_POSITION) {
            notifyItemChanged(oldPosition);
        }

        if (newPosition != NO_POSITION) {
            notifyItemChanged(newPosition);
        }
    }

    @Override
    public int getItemCount() {
        return NETWORKS_OFFSET + networkContracts.size() + 1;
    }

    private void onItemClicked(int position) {
        if (position == 0) {
            callbacks.onNoFiltersClicked();
            switchToPosition(NO_POSITION);
        } else if (position == getItemCount() - 1) {
            callbacks.onSettingsClicked();
        } else {
            callbacks.onNetworkClicked(networkContracts.get(position - NETWORKS_OFFSET).getId());
            switchToPosition(position);
        }
    }

    public void setNetworks(@NonNull SolidList<NetworkContract> networkContracts) {
        this.networkContracts = networkContracts;
        notifyDataSetChanged();
    }

    public class SideBarViewHolder extends RecyclerView.ViewHolder {

        @SuppressWarnings("NullableProblems") // Butterknife
        @BindView(R.id.sidebar_network_item_left_icon)
        @NonNull
        ImageView leftIconView;

        @SuppressWarnings("NullableProblems") // Butterknife
        @BindView(R.id.sidebar_network_item_right_icon)
        @NonNull
        ImageView rightIconView;

        @SuppressWarnings("NullableProblems") // Butterknife
        @BindView(R.id.sidebar_network_item_title)
        @NonNull
        TextView titleView;

        @NonNull
        private final PorterDuffColorFilter whiteFilter;

        @NonNull
        private final PorterDuffColorFilter grayIconFilter;

        @NonNull
        private final Drawable chevronDrawable;

        public SideBarViewHolder(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup parent
        ) {
            super(inflater.inflate(R.layout.sidebar_network_item, parent, false));
            ButterKnife.bind(this, itemView);
            whiteFilter = new PorterDuffColorFilter(getColor(R.color.white_color), SRC_ATOP);
            grayIconFilter = new PorterDuffColorFilter(getColor(R.color.sidebar_icon_color), SRC_ATOP);
            //noinspection ConstantConditions (valid resource)
            chevronDrawable = getDrawable(R.drawable.ic_chevron_right_24dp);

            itemView.setOnClickListener(v -> {
                SideBarAdapter.this.onItemClicked(getAdapterPosition());
            });
        }

        public void bind(@NonNull NetworkContract networkContract, boolean chosen) {
            resetState();

            final Drawable leftIcon = getDrawable(networkContract.getNetworkIconResource());
            final Drawable rightIcon = chevronDrawable;

            itemView.setClickable(true);
            if (chosen) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), networkContract.getBrandColorResourcePrimary()));
                if (leftIcon != null) {
                    leftIcon.setColorFilter(whiteFilter);
                }
                rightIcon.setColorFilter(whiteFilter);
                titleView.setTextColor(getColor(R.color.white_color));
            } else {
                itemView.setClickable(true);
                TypedValue outValue = new TypedValue();
                if (itemView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)) {
                    itemView.setBackgroundResource(outValue.resourceId);
                }

                if (leftIcon != null) {
                    leftIcon.setColorFilter(grayIconFilter);
                }
                rightIcon.setColorFilter(grayIconFilter);
            }

            leftIconView.setImageDrawable(leftIcon);
            rightIconView.setImageDrawable(rightIcon);
            titleView.setText(networkContract.getFullName());
        }

        public void bind(@DrawableRes int leftIcon, @DrawableRes int rightIcon, @StringRes int title) {
            resetState();
            itemView.setClickable(true);
            TypedValue outValue = new TypedValue();
            if (itemView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)) {
                itemView.setBackgroundResource(outValue.resourceId);
            }

            leftIconView.setImageDrawable(getDrawable(leftIcon));
            rightIconView.setImageDrawable(getDrawable(rightIcon));
            titleView.setText(title);
        }

        private void resetState() {
            leftIconView.setImageDrawable(null);
            rightIconView.setImageDrawable(null);
            itemView.setBackground(null);
            titleView.setText(null);
            titleView.setTextColor(getColor(R.color.sidebar_text_color));
        }

        @ColorInt
        private int getColor(@ColorRes int colorRes) {
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }

        @Nullable
        private Drawable getDrawable(@DrawableRes int res) {
            if (res != 0) {
                return ContextCompat.getDrawable(itemView.getContext(), res);
            } else {
                return null;
            }
        }
    }
}
