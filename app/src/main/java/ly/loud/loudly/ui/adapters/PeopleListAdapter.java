package ly.loud.loudly.ui.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.ui.adapters.holders.BindingViewHolder;
import ly.loud.loudly.ui.adapters.holders.ListItem;
import ly.loud.loudly.ui.adapters.holders.ViewHolderDelimiter;
import ly.loud.loudly.ui.adapters.holders.ViewHolderFactory;
import ly.loud.loudly.ui.adapters.holders.ViewHolderPerson;

import static ly.loud.loudly.ui.adapters.holders.ItemTypes.ItemType;
import static ly.loud.loudly.ui.adapters.holders.ItemTypes.PERSON;

public class PeopleListAdapter extends RecyclerView.Adapter<BindingViewHolder>
        implements ViewHolderPerson.ViewHolderPersonClickListener {

    @NonNull
    private List<ListItem> items = new ArrayList<>();

    @Override
    @ItemType
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Nullable
    private PeopleListClickListener clickListener;

    @Override
    public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ItemType int viewType) {
        switch (viewType) {
            case PERSON:
                return ViewHolderPerson.provideViewHolderWithListener(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        this
                );
            default:    // DELIMITER
                return ViewHolderFactory.provideViewHolder(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        viewType
                );
        }
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
     *
     * @param persons - list of persons to add
     * @param network - network which delimiter will be inserted
     */
    public void addPersons(@NonNull List<Person> persons, @Network int network) {
        int positionStart = items.size() - 1;

        items.add(new NetworkDelimiter(network));
        items.addAll(persons);

        notifyItemRangeChanged(positionStart, persons.size() + 1);
    }

    public void setClickListener(@Nullable PeopleListClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onPersonClick(int position) {
        if (clickListener != null && (items.get(position) instanceof Person)) {
            clickListener.onPersonClick((Person) items.get(position));
        }
    }

    public interface PeopleListClickListener {
        void onPersonClick(@NonNull Person person);
    }
}
