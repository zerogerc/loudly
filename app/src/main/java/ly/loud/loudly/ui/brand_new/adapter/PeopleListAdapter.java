package ly.loud.loudly.ui.brand_new.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.new_base.Networks.Network;
import ly.loud.loudly.new_base.Person;
import ly.loud.loudly.ui.adapter.NetworkDelimiter;

import static ly.loud.loudly.ui.brand_new.ItemTypes.ItemType;
import static ly.loud.loudly.ui.brand_new.ItemTypes.PERSON;

public class PeopleListAdapter extends RecyclerView.Adapter<BindingViewHolder> {

    @NonNull
    private List<ListItem> items = new ArrayList<>();

    @Override
    @ItemType
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ItemType int viewType) {
        return ViewHolderFactory.provideViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case PERSON:
                ((ViewHolderPerson) holder).bind(((Person) items.get(position)));
                break;
            default: // DELIMITER
                ((ViewHolderDelimiter) holder).bind(((NetworkDelimiter) items.get(position)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Adds delimiter corresponding to the given network following by list of given persons.
     * @param persons - list of persons to add
     * @param network - network which delimiter will be inserted
     */
    public void addPersons(@NonNull List<Person> persons, @Network int network) {
        int positionStart = items.size() - 1;

        items.add(new NetworkDelimiter(network));
        items.addAll(persons);

        notifyItemRangeChanged(positionStart, persons.size() + 1);
    }
}
