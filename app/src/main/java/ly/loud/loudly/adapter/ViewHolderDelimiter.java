package ly.loud.loudly.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ly.loud.loudly.Loudly;
import ly.loud.loudly.R;
import util.Utils;

/**
 * Created by ZeRoGerc on 25.12.15.
 */
public class ViewHolderDelimiter extends ViewHolder {
    ImageView icon;

    public ViewHolderDelimiter(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_delimeter, parent, false));

        Item item = new NetworkDelimiter();

        icon = (ImageView) itemView.findViewById(R.id.people_list_delimeter_icon);
        refresh(item);
    }

    @Override
    public void refresh(Item item) {
        NetworkDelimiter delimiter = ((NetworkDelimiter) item);

        Glide.with(Loudly.getContext())
                .load(Utils.getResourceByNetwork(delimiter.getNetwork()))
                .override(Utils.dpToPx(48), Utils.dpToPx(48))
                .fitCenter()
                .into(icon);

    }
}