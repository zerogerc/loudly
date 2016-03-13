package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.R;
import ly.loud.loudly.ui.adapter.AfterLoadAdapter;
import ly.loud.loudly.ui.adapter.Item;
import ly.loud.loudly.ui.adapter.ViewHolder;
import ly.loud.loudly.ui.adapter.ViewHolderComment;
import ly.loud.loudly.util.AttachableReceiver;
import ly.loud.loudly.util.Broadcasts;

/**
 * Created by ZeRoGerc on 06.12.15.
 * ITMO University
 */
public class PeopleListFragment extends Fragment {
    private static final int COMMENTS = -1;
    private static int depth = 0;
    static GetPersonReceiver getPersonReceiver;

    private int requestType = Tasks.LIKES;
    private SingleNetwork element;

    LinkedList<Item> items = new LinkedList<>();
    RecyclerView recyclerView;
    AfterLoadAdapter recyclerViewAdapter;
    LinearLayoutManager layoutManager;

    private ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_fragment, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.people_list_recycler_view);

//        //TODO: remove this
//        items.add(new Post());
//        for (int i = 0; i < 1000; i++) {
//            if (i % 2 == 0) {
//                items.add(new NetworkDelimiter(Networks.FB));
//            } else {
//                items.add(new Person("Vlad", "Sazanovich", null, Networks.VK));
//            }
//        }
        recyclerViewAdapter = new AfterLoadAdapter(items, getActivity()) {
            @Override
            public void onFirstItemAppeared() {
                hideProgress();
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, final int position) {
                if (holder instanceof ViewHolderComment) {
                    ((ViewHolderComment) holder).setLikesOnClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PeopleListFragment.showPersons(getActivity(), ((Comment) items.get(position)), Tasks.LIKES);
                        }
                    });
                }
                super.onBindViewHolder(holder, position);
            }
        };

        layoutManager = new LinearLayoutManager(Loudly.getContext());

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        progress = ((ProgressBar) rootView.findViewById(R.id.people_list_progress));

        int text;
        switch (requestType) {
            case Tasks.LIKES:
                text = R.string.people_fragment_title_likes;
                break;
            case Tasks.SHARES:
                text = R.string.people_fragment_title_shares;
                break;
            default:
                text = R.string.people_fragment_title_comments;
                break;
        }
        ((TextView) rootView.findViewById(R.id.people_list_title)).setText(text);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            items.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private static final String TAG = "FRAGMENT";

    @Override
    public void onResume() {
        super.onResume();
        if (getPersonReceiver != null) {
            getPersonReceiver.attachAdapter(recyclerViewAdapter);
        }
        Log.e(TAG, "resume: " + depth);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        depth--;
        if (depth == 0) {
            getPersonReceiver.stop();
            getPersonReceiver = null;
        }
        Log.e(TAG, "onDetach: " + depth);
        super.onDetach();
    }

    static class CustomLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = true;

        public CustomLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = flag;
        }

        @Override
        public boolean canScrollVertically() {
            return isScrollEnabled && super.canScrollVertically();
        }
    }

    static class CustomRecyclerViewListener extends RecyclerView.OnScrollListener {
        private PeopleListFragment fragment;

        public CustomRecyclerViewListener(PeopleListFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    }

    private static class GetPersonReceiver extends AttachableReceiver {
        WeakReference<AfterLoadAdapter> adapter;
        public GetPersonReceiver(Context context) {
            super(context, Broadcasts.GET_PERSONS);
        }

        public void attachAdapter(AfterLoadAdapter adapter) {
            this.adapter = new WeakReference<>(adapter);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            int broadcastDepth = message.getIntExtra(Broadcasts.ID_FIELD, depth);
            if (broadcastDepth != depth) {
                return;
            }
            AfterLoadAdapter recyclerViewAdapter = adapter.get();
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
                    if (depth == 0) {
                        stop();
                        getPersonReceiver = null;
                    }
                    break;
            }
        }
    }

    private static void show(Activity activity, PeopleListFragment fragment) {
        depth++;
        if (getPersonReceiver == null){
            getPersonReceiver = new GetPersonReceiver(activity);
        }
        if (fragment.requestType == COMMENTS) {
            Tasks.CommentsGetter task = new Tasks.CommentsGetter(depth, fragment.element,
                    fragment.items, Loudly.getContext().getWraps());
            task.execute();
        } else {
            Tasks.PersonGetter task = new Tasks.PersonGetter(depth, fragment.element,
                    fragment.requestType, fragment.items, Loudly.getContext().getWraps());
            task.execute();
        }

        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        showProgress();
        super.onViewCreated(view, savedInstanceState);
    }

    public static PeopleListFragment showPersons(Activity activity, SingleNetwork element, int type) {
        PeopleListFragment newFragment = new PeopleListFragment();
        newFragment.element = element;
        newFragment.requestType = type;

        show(activity, newFragment);
        return newFragment;
    }

    public static PeopleListFragment showComments(Activity activity, SingleNetwork element) {
        PeopleListFragment newFragment = new PeopleListFragment();
        newFragment.element = element;
        newFragment.requestType = COMMENTS;

        show(activity, newFragment);
        return newFragment;
    }

    public void hideProgress() {
        this.progress.setVisibility(View.GONE);
    }

    public void showProgress() {
        this.progress.setVisibility(View.VISIBLE);
    }
}
