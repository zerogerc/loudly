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
public class ViewHolderPerson extends ViewHolder<Person> {
    private ImageView icon;
    private TextView name;

    public ViewHolderPerson(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.list_person, parent, false));

        Person person = new Person();

        icon = (ImageView) itemView.findViewById(R.id.people_list_person_avatar);
        name = (TextView) itemView.findViewById(R.id.people_list_person_name);
        refresh(person);
    }

    @Override
    public void refresh(final Person person) {
        Utils.loadAvatar(person, icon);
        Utils.loadName(person, name);
    }
}
