package ly.loud.loudly.ui.adapter.person;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.ui.adapter.ViewHolder;
import ly.loud.loudly.util.Utils;


/**
 * ViewHolder for representing person in {@link android.support.v7.widget.RecyclerView}
 */
public class ViewHolderPerson extends ViewHolder<Person> {

    private ViewHolderPersonPresenter presenter;

    @BindView(R.id.people_list_person_avatar) ImageView icon;
    @BindView(R.id.people_list_person_name) TextView name;

    private Person currentPerson;

    public ViewHolderPerson(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.list_person, parent, false));

        presenter = new ViewHolderPersonPresenter(getActivity());
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void refresh(final Person person) {
        currentPerson = person;

        Utils.loadAvatar(person, icon);
        Utils.loadName(person, name);
    }

    @OnClick (R.id.people_list_person_layout)
    public void openUrl() {
        if (currentPerson != null) {
            presenter.openWebView(currentPerson.getPhotoUrl());
        }
    }
}
