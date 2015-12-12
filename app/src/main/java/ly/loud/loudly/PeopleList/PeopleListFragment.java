package ly.loud.loudly.PeopleList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import base.SingleNetwork;
import base.Tasks;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.MainActivity;
import ly.loud.loudly.R;
import ly.loud.loudly.RecyclerViewAdapter;
import util.AttachableReceiver;
import util.Broadcasts;

/**
 * Created by ZeRoGerc on 06.12.15.
 */
public class PeopleListFragment extends Fragment {
    private static final int COMMENTS = -1;
    private static int depth = 0;
    static GetPersonReceiver getPersonReceiver;

    private View rootView;

    private int requestType = Tasks.LIKES;
    private SingleNetwork element;
    private int paddingTopInitial;
    private int distanceTop = 0;

    LinkedList<Item> items = new LinkedList<>();
    RecyclerView recyclerView;
    PeopleListAdapter recyclerViewAdapter;
    LinearLayoutManager layoutManager;
//    CustomRecyclerViewListener scrollListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.people_list, container, false);
        paddingTopInitial = rootView.getPaddingTop();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.people_list_recycler_view);
        recyclerViewAdapter = new PeopleListAdapter(items, getActivity());
//        recyclerView.setHasFixedSize(true); /// HERE
        layoutManager = new LinearLayoutManager(Loudly.getContext());

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

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
            rootView.setPadding(rootView.getPaddingLeft(), paddingTopInitial, rootView.getPaddingRight(), rootView.getPaddingBottom());
            distanceTop = paddingTopInitial;
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
        WeakReference<PeopleListAdapter> adapter;
        public GetPersonReceiver(Context context) {
            super(context, Broadcasts.POST_GET_PERSONS);
        }

        public void attachAdapter(PeopleListAdapter adapter) {
            this.adapter = new WeakReference<>(adapter);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            int broadcastDepth = message.getIntExtra(Broadcasts.ID_FIELD, depth);
            if (broadcastDepth != depth) {
                return;
            }
            PeopleListAdapter recyclerViewAdapter = adapter.get();
            switch (status) {
                case Broadcasts.PROGRESS:
                    recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.ERROR:
                    String error = message.getStringExtra(Broadcasts.ERROR_FIELD);
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    break;
                case Broadcasts.FINISHED:
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (depth == 0) {
                        stop();
                        getPersonReceiver = null;
                    }
                    break;
            }
        }
    }

    public void show() {
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        ft.show(this);
        ft.commit();
    }

    public void fillPersons(SingleNetwork element, int type) {
        this.element = element;
        this.requestType = type;
    }

    public void fillComments(SingleNetwork element) {
        this.element = element;
        this.requestType = COMMENTS;
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
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
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

    public LinkedList<Item> getItems() {
        return items;
    }

    public void setItems(LinkedList<Item> items) {
        this.items = items;
    }
}
