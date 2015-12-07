package ly.loud.loudly.PeopleList;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

import base.Post;
import base.Tasks;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.MainActivity;
import ly.loud.loudly.R;
import util.AttachableReceiver;
import util.Broadcasts;

/**
 * Created by ZeRoGerc on 06.12.15.
 */
public class PeopleListFragment extends Fragment {
    private View rootView;
    private int requestType = Tasks.LIKES;

    private Post post;
    static public LinkedList<Item> persons = new LinkedList<>();
    RecyclerView recyclerView;
    PeopleListAdapter recyclerViewAdapter;
    static AttachableReceiver getPersonReceiver;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.people_list, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.people_list_recycler_view);
        recyclerViewAdapter = new PeopleListAdapter(persons);
        recyclerView.setHasFixedSize(true); /// HERE
        LinearLayoutManager layoutManager = new LinearLayoutManager(Loudly.getContext());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            persons.clear();
            getPersonReceiver = new GetPersonReceiver(getActivity());

            Tasks.PersonGetter task = new Tasks.PersonGetter(post, requestType, persons,
                    Loudly.getContext().getWraps());
            task.execute();
        } else {
            persons.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    static class GetPersonReceiver extends AttachableReceiver {
        public GetPersonReceiver(Context context) {
            super(context, Broadcasts.POST_GET_PERSONS);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            String status = message.getStringExtra(Broadcasts.STATUS_FIELD);
            final MainActivity activity = ((MainActivity) context);
            switch (status) {
                case Broadcasts.PROGRESS:
                    activity.peopleListFragment.recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.FINISHED:
                    activity.peopleListFragment.recyclerViewAdapter.notifyDataSetChanged();
                    stop();
                    break;
            }
        }
    }

    public void showPersons(Post post, int type) {
        this.post = post;
        this.requestType = type;
        ((MainActivity) getActivity()).floatingActionButton.hide();
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        ft.show(this);
        ft.commit();
    }
}
