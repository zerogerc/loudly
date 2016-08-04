package ly.loud.loudly.ui.brand_new.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.ui.adapter.NetworkDelimiter;
import ly.loud.loudly.util.Utils;

public class ViewHolderDelimiter extends BindingViewHolder<NetworkDelimiter> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_delimeter_icon)
    @NonNull
    ImageView icon;


    public ViewHolderDelimiter(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_delimeter, parent, false));

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@NonNull NetworkDelimiter delimiter) {
        Glide.with(Loudly.getContext())
                .load(Utils.getResourceByNetwork(delimiter.getNetwork()))
                .override(Utils.dpToPx(48), Utils.dpToPx(48))
                .fitCenter()
                .into(icon);
    }
}
