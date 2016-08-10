package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.util.Utils;

public class ViewHolderPerson extends BindingViewHolder<Person> {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_person_avatar)
    @NonNull
    ImageView icon;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_person_name)
    @NonNull
    TextView name;

    public ViewHolderPerson(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_person, parent, false));

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(@NonNull Person person) {
        Utils.loadAvatar(person, icon);
        Utils.loadName(person, name);
    }
}
