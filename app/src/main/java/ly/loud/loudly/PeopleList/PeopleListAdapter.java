package ly.loud.loudly.PeopleList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import base.Person;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.R;
import util.Utils;
import util.picasso.CircleTransform;

/**
 * Created by ZeRoGerc on 07.12.15.
 */
public class PeopleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Item> items;

    void refreshFields(final RecyclerView.ViewHolder viewHolder, final Item item) {
        if (item instanceof Person) {
            ViewHolderPerson holder = (ViewHolderPerson) viewHolder;
            Person person = (Person) item;
            if (person.getPhotoUrl().length() != 0) {
                Picasso.with(Loudly.getContext())
                        .load(person.getPhotoUrl())
                        .transform(new CircleTransform())
                        .resize(Utils.dpToPx(48), Utils.dpToPx(48))
                        .centerInside()
                        .into(holder.icon);
            } else {
                Picasso.with(Loudly.getContext())
                        .load(R.mipmap.ic_launcher)
                        .resize(Utils.dpToPx(48), Utils.dpToPx(48))
                        .centerInside()
                        .into(holder.icon);
            }

            String text = person.getFirstName() + " " + person.getLastName();
            holder.name.setText(text);
        } else {
            ViewHolderDelimeter holder = (ViewHolderDelimeter) viewHolder;
            NetworkDelimiter delimeter = ((NetworkDelimiter) item);
            Picasso.with(Loudly.getContext())
                    .load(Utils.getResourceByNetwork(delimeter.getNetwork()))
                    .resize(Utils.dpToPx(48), Utils.dpToPx(48))
                    .centerInside()
                    .into(holder.icon);
        }
    }

    public PeopleListAdapter(List<Item> items) {
        this.items = items;
    }


    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Person) {
            return Item.PERSON;
        } else {
            return Item.DELIMITER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Item.PERSON: {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_person, parent, false);
                return new ViewHolderPerson(v, new Person("", "", "", -1));
            }
            default: { //Item.DELIMITER
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_delimeter, parent, false);
                return new ViewHolderDelimeter(v, new NetworkDelimiter(-1));
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        refreshFields(holder, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolderDelimeter extends RecyclerView.ViewHolder {
        ImageView icon;

        public ViewHolderDelimeter(View itemView, Item item) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.people_list_delimeter_icon);
            refreshFields(this, item);
        }

    }

    class ViewHolderPerson extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolderPerson(View itemView, Item item) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.people_list_person_avatar);
            name = (TextView) itemView.findViewById(R.id.people_list_person_name);
            refreshFields(this, item);
        }

    }
}
