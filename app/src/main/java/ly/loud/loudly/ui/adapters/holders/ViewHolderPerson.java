package ly.loud.loudly.ui.adapters.holders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @Nullable
    private ViewHolderPersonClickListener clickListener;

    @NonNull
    public static ViewHolderPerson provideViewHolderWithListener(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup parent,
            @NonNull ViewHolderPersonClickListener clickListener) {
        ViewHolderPerson viewHolderPerson = new ViewHolderPerson(inflater, parent);
        viewHolderPerson.setClickListener(clickListener);
        return viewHolderPerson;
    }

    public ViewHolderPerson(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_person, parent, false));

        ButterKnife.bind(this, itemView);
    }

    @OnClick(R.id.list_item_person_layout)
    void onPersonClick() {
        if (clickListener != null) {
            clickListener.onPersonClick(getAdapterPosition());
        }
    }

    @Override
    public void bind(@NonNull Person person) {
        Utils.loadAvatar(person, icon);
        Utils.loadName(person, name);
    }

    public void setClickListener(@Nullable ViewHolderPersonClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ViewHolderPersonClickListener {
        void onPersonClick(int position);
    }
}
