package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.ui.adapters.NetworkDelimiter;
import ly.loud.loudly.util.Utils;

public class ViewHolderDelimiter extends BindingViewHolder<NetworkDelimiter> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_delimeter_icon)
    @NonNull
    ImageView icon;

    @Px
    int iconSize;

    public ViewHolderDelimiter(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_delimeter, parent, false));

        ButterKnife.bind(this, itemView);
        iconSize = itemView.getResources().getDimensionPixelSize(R.dimen.standard_icon_size_48);
    }

    @Override
    public void bind(@NonNull NetworkDelimiter delimiter) {
        Glide.with(itemView.getContext())
                .load(Utils.getResourceByNetwork(delimiter.getNetwork()))
                .override(iconSize, iconSize)
                .fitCenter()
                .into(icon);
    }
}
