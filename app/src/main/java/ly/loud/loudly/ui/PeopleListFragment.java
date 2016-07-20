package ly.loud.loudly.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.PeopleGetterModel;
import ly.loud.loudly.application.models.PeopleGetterModel.PersonsFromNetwork;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.ui.adapter.AfterLoadAdapter;
import ly.loud.loudly.ui.adapter.Item;
import ly.loud.loudly.ui.adapter.NetworkDelimiter;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by ZeRoGerc on 06.12.15.
 * ITMO University
 */
public class PeopleListFragment extends DialogFragment {

    @Inject
    PeopleGetterModel peopleGetterModel;

    @BindView(R.id.people_list_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.people_list_progress)
    ProgressBar progress;

    @BindView(R.id.people_list_title)
    TextView title;

    private static final String SINGLE_NETWORK_KEY = "single_network";
    private static final String REQUEST_TYPE_KEY = "request_type";

    private int requestType;

    @NonNull
    private SingleNetwork element;

    @NonNull
    private final LinkedList<Item> items = new LinkedList<>();

    public static PeopleListFragment newInstance(@NonNull SingleNetwork element, int requestType) {
        PeopleListFragment frag = new PeopleListFragment();
        Bundle args = new Bundle();
        args.putParcelable(SINGLE_NETWORK_KEY, element);
        args.putInt(REQUEST_TYPE_KEY, requestType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.list_fragment, null);


        ((Loudly) getActivity().getApplication()).getAppComponent().inject(this);
        ButterKnife.bind(this, rootView);

        //noinspection ConstantConditions
        element = getArguments().getParcelable(SINGLE_NETWORK_KEY);
        requestType = getArguments().getInt(REQUEST_TYPE_KEY);

        int titleRes = R.string.people_fragment_title_likes;
        if (requestType == Tasks.SHARES) {
            titleRes = R.string.people_fragment_title_shares;
        }
        title.setText(titleRes);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new AfterLoadAdapter(items, getActivity()) {
                                    @Override
                                    public void onFirstItemAppeared() {
                                        progress.setVisibility(View.GONE);
                                    }
                                }
        );
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        progress.setVisibility(View.VISIBLE);

        builder.setView(rootView);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (items.isEmpty()) { // First run
            peopleGetterModel.getPersonsByType(element, requestType, Loudly.getContext().getWraps())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::loadItems);
        }
    }


    private void loadItems(@NonNull List<PersonsFromNetwork> persons) {
        for (PersonsFromNetwork list : persons) {
            if (!list.persons.isEmpty()) {
                items.add(new NetworkDelimiter(list.network));
                items.addAll(list.persons);
            }
        }

        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
