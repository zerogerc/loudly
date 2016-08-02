package ly.loud.loudly.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.R;
import ly.loud.loudly.util.Utils;

/**
 * Created by ZeRoGerc on 25.12.15.
 * ITMO University
 */

public class ViewHolderDelimiter extends ViewHolder<NetworkDelimiter> {
    private ImageView icon;

    public ViewHolderDelimiter(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.list_delimeter, parent, false));

        NetworkDelimiter delimiter = new NetworkDelimiter();

        icon = (ImageView) itemView.findViewById(R.id.people_list_delimeter_icon);
        refresh(delimiter);
    }

    @Override
    public void refresh(final NetworkDelimiter delimiter) {
        Glide.with(Loudly.getContext())
                .load(Utils.getResourceByNetwork(delimiter.getNetwork()))
                .override(Utils.dpToPx(48), Utils.dpToPx(48))
                .fitCenter()
                .into(icon);
    }
}
