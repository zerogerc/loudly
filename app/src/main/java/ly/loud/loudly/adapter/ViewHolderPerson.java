package ly.loud.loudly.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import base.Person;
import ly.loud.loudly.R;
import util.Utils;

/**
 * Created by ZeRoGerc on 25.12.15.
 */
public class ViewHolderPerson extends ViewHolder {
    ImageView icon;
    TextView name;

    public ViewHolderPerson(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_person, parent, false));

        Item item = new Person();

        icon = (ImageView) itemView.findViewById(R.id.people_list_person_avatar);
        name = (TextView) itemView.findViewById(R.id.people_list_person_name);
        refresh(item);
    }

    @Override
    public void refresh(Item item) {
        Person person = ((Person) item);

        Utils.loadAvatar(person, icon);
        Utils.loadName(person, name);
    }
}
