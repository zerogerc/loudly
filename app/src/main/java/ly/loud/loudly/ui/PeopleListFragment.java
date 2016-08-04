package ly.loud.loudly.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.application.models.GetterModel.RequestType;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.ui.brand_new.adapter.PeopleListAdapter;
import rx.Observable;
import rx.schedulers.Schedulers;

import static ly.loud.loudly.application.models.GetterModel.SHARES;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class PeopleListFragment extends DialogFragment {

    private static final String SINGLE_NETWORK_KEY = "single_network";
    private static final String REQUEST_TYPE_KEY = "request_type";

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    GetterModel getterModel;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_recycler_view)
    @NonNull
    RecyclerView recyclerView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_progress)
    @NonNull
    ProgressBar progress;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.people_list_title)
    @NonNull
    TextView title;

    @RequestType
    private int requestType;

    @SuppressWarnings("NullableProblems") // onCreateDialog
    @NonNull
    private ArrayList<? extends SingleNetworkElement> instances;

    @SuppressWarnings("NullableProblems") // onCreateDialog
    @NonNull
    private PeopleListAdapter adapter;

    /**
     * Indicates whether this fragment created view for the first time.
     * Should be accessed only from main thread.
     */
    private boolean firstRun = true;

    public static PeopleListFragment newInstance(@NonNull ArrayList<? extends SingleNetworkElement> element,
                                                 @RequestType int requestType) {
        PeopleListFragment frag = new PeopleListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SINGLE_NETWORK_KEY, element);
        args.putInt(REQUEST_TYPE_KEY, requestType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Loudly.getContext().getAppComponent().inject(this);

        //noinspection ConstantConditions
        instances = getArguments().getParcelableArrayList(SINGLE_NETWORK_KEY);
        requestType = getArguments().getInt(REQUEST_TYPE_KEY);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.list_fragment, null);

        ButterKnife.bind(this, rootView);

        int titleRes = R.string.people_fragment_title_likes;
        if (requestType == SHARES) {
            titleRes = R.string.people_fragment_title_shares;
        }
        title.setText(titleRes);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new PeopleListAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        progress.setVisibility(View.VISIBLE);

        builder.setView(rootView);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (firstRun) { // First run
            Observable.from(instances)
                    .flatMap(instance -> getterModel.getPersonsByType(instance, requestType))
                    .subscribeOn(Schedulers.io())
                    .observeOn(mainThread())
                    .doOnNext(personsFromNetwork -> {
                        if (!personsFromNetwork.persons.isEmpty()) {
                            adapter.addPersons(personsFromNetwork.persons, personsFromNetwork.network);
                        }
                    })
                    .doOnError(throwable -> {
                        // TODO: show user that something goes wrong
                    })
                    .doOnCompleted(() -> {
                        firstRun = false;
                        progress.setVisibility(View.GONE);
                    })
                    .subscribe();
        }
    }
}
