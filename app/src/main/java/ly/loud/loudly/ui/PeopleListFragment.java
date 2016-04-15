package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.ui.adapter.AfterLoadAdapter;
import ly.loud.loudly.ui.adapter.Item;
import ly.loud.loudly.util.AttachableReceiver;
import ly.loud.loudly.util.Broadcasts;

/**
 * Created by ZeRoGerc on 06.12.15.
 * ITMO University
 */
public class PeopleListFragment extends DialogFragment {
    private static final String TAG = "People List Fragment";
    private static final String TITLE_KEY = "title";

    private static final int COMMENTS = -1;
    private GetPersonReceiver getPersonReceiver;

    private int requestType = Tasks.LIKES;
    private SingleNetwork element;

    private LinkedList<Item> items = new LinkedList<>();
    private RecyclerView recyclerView;

    private ProgressBar progress;

    public static PeopleListFragment newInstance(int titleResource) {
        PeopleListFragment frag = new PeopleListFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleResource);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View rootView = inflater.inflate(R.layout.list_fragment, null);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.people_list_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new AfterLoadAdapter(items, getActivity()) {
                                    @Override
                                    public void onFirstItemAppeared() {
                                        hideProgress();
                                    }
                                }
        );
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        progress = ((ProgressBar) rootView.findViewById(R.id.people_list_progress));
        showProgress();

        ((TextView) rootView.findViewById(R.id.people_list_title)).setText(getArguments().getInt(TITLE_KEY));

        builder.setView(rootView);
        return builder.create();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            items.clear();
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPersonReceiver != null) {
            getPersonReceiver.attachAdapter(recyclerView.getAdapter());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        if (getPersonReceiver != null) {
            getPersonReceiver.stop();
            getPersonReceiver = null;
        }
        super.onDetach();
    }

    private static class GetPersonReceiver extends AttachableReceiver {
        WeakReference<RecyclerView.Adapter> adapter;

        public GetPersonReceiver(Context context) {
            super(context, Broadcasts.GET_PERSONS);
        }

        public void attachAdapter(RecyclerView.Adapter adapter) {
            this.adapter = new WeakReference<>(adapter);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            RecyclerView.Adapter recyclerViewAdapter = adapter.get();
            switch (status) {
                case Broadcasts.PROGRESS:
                    recyclerViewAdapter.notifyItemInserted(recyclerViewAdapter.getItemCount());
                    break;
                case Broadcasts.ERROR:
                    int network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    int kind = message.getIntExtra(Broadcasts.ERROR_KIND, -1);
                    String error = "Can't load people from " + Networks.nameOfNetwork(network) + ": ";
                    switch (kind) {
                        case Broadcasts.NETWORK_ERROR:
                            error += "no internet connection";
                            break;
                        case Broadcasts.INVALID_TOKEN:
                            error += "lost connection to network";
                            break;
                    }
                    MainActivity mainActivity = (MainActivity) context;
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
                    break;
                case Broadcasts.FINISHED:
                    stop();
                    break;
            }
        }
    }

    private static void show(Activity activity, PeopleListFragment fragment) {
        fragment.getPersonReceiver = new GetPersonReceiver(activity);
        if (fragment.requestType == COMMENTS) {
            Tasks.CommentsGetter task = new Tasks.CommentsGetter(fragment.element,
                    fragment.items, Loudly.getContext().getWraps());
            task.execute();
        } else {
            Tasks.PersonGetter task = new Tasks.PersonGetter(fragment.element,
                    fragment.requestType, fragment.items, Loudly.getContext().getWraps());
            task.execute();
        }

        fragment.show(activity.getFragmentManager(), TAG);
    }

    public static PeopleListFragment showPersons(Activity activity, SingleNetwork element, int type) {
        int res = R.string.people_fragment_title_likes;
        if (type == Tasks.SHARES) {
            res = R.string.people_fragment_title_shares;
        }

        PeopleListFragment newFragment = PeopleListFragment.newInstance(res);

        newFragment.element = element;
        newFragment.requestType = type;

        show(activity, newFragment);
        return newFragment;
    }

    //TODO: remove this after creating special activity for such tasks
    public static PeopleListFragment showComments(Activity activity, SingleNetwork element) {
        PeopleListFragment newFragment = PeopleListFragment.newInstance(R.string.people_fragment_title_comments);
        newFragment.element = element;
        newFragment.requestType = COMMENTS;

        show(activity, newFragment);
        return newFragment;
    }

    public void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    public void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }
}
