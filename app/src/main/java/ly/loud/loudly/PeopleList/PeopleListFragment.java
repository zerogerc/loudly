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
import android.widget.Toast;

import java.util.LinkedList;

import base.SingleNetwork;
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
    private static final int COMMENTS = -1;
    private View rootView;

    private int requestType = Tasks.LIKES;
    private SingleNetwork element;
    private int paddingTopInitial;
    private int distanceTop = 0;
    private boolean hasListener = false;

    private LinkedList<Item> items = new LinkedList<>();
    RecyclerView recyclerView;
    PeopleListAdapter recyclerViewAdapter;
    LinearLayoutManager layoutManager;
//    CustomRecyclerViewListener scrollListener;

    static AttachableReceiver getPersonReceiver;

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
//        scrollListener = new CustomRecyclerViewListener(this);
//        recyclerView.addOnScrollListener(scrollListener);

//        final PeopleListFragment fragment = this;
//        recyclerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() != MotionEvent.ACTION_MOVE || event.getHistorySize() == 0)
//                    return false;
//
//
//                int previousY = ((int) event.getHistoricalY(0));
//                int dy = ((int) event.getY()) - previousY;
//                Log.d("TAG", Integer.toString(dy));
//
//                if (distanceTop == 0) {
//                    if (!hasListener) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//
//                }
//
//                if (distanceTop > 0 && dy < 0) {
//                    distanceTop = Math.max(0, distanceTop + dy);
//                    rootView.setPadding(rootView.getPaddingLeft(), distanceTop,
//                            rootView.getPaddingRight(), rootView.getPaddingBottom());
//                    return true;
//                }
//
//                return false;
//            }
//        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            items.clear();
            hasListener = false;

            getPersonReceiver = new GetPersonReceiver(getActivity());

            if (requestType == COMMENTS) {
                Tasks.CommentsGetter task = new Tasks.CommentsGetter(element, items, Loudly.getContext().getWraps());
                task.execute();
            } else {
                Tasks.PersonGetter task = new Tasks.PersonGetter(element, requestType, items,
                        Loudly.getContext().getWraps());
                task.execute();
            }
        } else {
            rootView.setPadding(rootView.getPaddingLeft(), paddingTopInitial, rootView.getPaddingRight(), rootView.getPaddingBottom());
            distanceTop = paddingTopInitial;
            items.clear();
            recyclerViewAdapter.notifyDataSetChanged();
        }
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

    static class GetPersonReceiver extends AttachableReceiver {
        public GetPersonReceiver(Context context) {
            super(context, Broadcasts.POST_GET_PERSONS);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            final MainActivity activity = ((MainActivity) context);
            switch (status) {
                case Broadcasts.PROGRESS:
                    activity.peopleListFragment.recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.ERROR:
                    String error = message.getStringExtra(Broadcasts.ERROR_FIELD);
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    stop();
                    break;
                case Broadcasts.FINISHED:
                    activity.peopleListFragment.recyclerViewAdapter.notifyDataSetChanged();
                    stop();
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

    public void showPersons(SingleNetwork element, int type) {
        fillPersons(element, type);
        show();
    }

    public void showComments(SingleNetwork element) {
        fillComments(element);
        show();
    }

    public LinkedList<Item> getItems() {
        return items;
    }

    public void setItems(LinkedList<Item> items) {
        this.items = items;
    }
}
