package ly.loud.loudly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.LinkedList;

import base.Tasks;
import base.says.Post;
import ly.loud.loudly.PeopleList.PeopleListFragment;
import util.AttachableReceiver;
import util.Broadcasts;
import util.UIAction;
import util.Utils;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    static public LinkedList<Post> posts = new LinkedList<>();

    RecyclerView recyclerView;
    public RecyclerViewAdapter recyclerViewAdapter;
    public FloatingActionButton floatingActionButton;
    private PostCreateFragment newPostFragment;

    private FrameLayout background;

    static final int LOAD_POSTS_RECEIVER = 0;
    static final int POST_UPLOAD_RECEIVER = 1;
    static final int GET_INFO_RECEIVER = 2;
    static final int POST_DELETE_RECEIVER = 3;
    static final int RECEIVER_COUNT = 4;

    static AttachableReceiver[] receivers = null;
    private static Tasks.LoadPostsTask loadPosts = null;

    private static MainActivity self;
    public static void executeOnMain(final UIAction action) {
        if (self != null) {
            self.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    action.execute(self);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (posts.isEmpty() && loadPosts == null) {
            // Loading posts

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.main_activity_progress);
            progressBar.setVisibility(View.VISIBLE);

            receivers[LOAD_POSTS_RECEIVER] = new PostLoadReceiver(this);

            loadPosts = new Tasks.LoadPostsTask(posts, Loudly.getContext().getTimeInterval(),
                    Loudly.getContext().getWraps());
            loadPosts.execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.activity_main);

        if (receivers == null) {
            receivers = new AttachableReceiver[RECEIVER_COUNT];
        }
        for (AttachableReceiver receiver : receivers) {
            if (receiver != null) {
                receiver.attach(this);
            }
        }

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int count = getFragmentManager().getBackStackEntryCount();
                if (count > 0) {
                    floatingActionButton.hide();
                    background.setAlpha(1);
                    background.getBackground().setAlpha(100);
                    background.setClickable(true);
                }
                if (count == 0) {
                    floatingActionButton.show();
                    background.setAlpha(0);
                    background.setClickable(false);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);

        FragmentManager manager = getFragmentManager();
        newPostFragment = ((PostCreateFragment) manager.findFragmentById(R.id.new_post_fragment));
        findViewById(R.id.new_post_fragment).getBackground().setAlpha(100);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(newPostFragment);
        ft.commit();

        PeopleListFragment peopleListFragment = new PeopleListFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, peopleListFragment).commit();
        getFragmentManager().beginTransaction().hide(peopleListFragment).commit();

        background = ((FrameLayout) findViewById(R.id.main_background));
        background.setAlpha(0);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);



        setRecyclerView();
    }

    static class CustomRecyclerViewListener extends RecyclerView.OnScrollListener {
        private FloatingActionButton fab;
        private boolean isShowFab = true;
        private boolean nowAnimating = false;
        private int animDuration = 200;
        private ObjectAnimator fabAnimSlideDown;
        private ObjectAnimator fabAnimSlideUp;

        public CustomRecyclerViewListener(FloatingActionButton fab, int screenHeight) {
            this.fab = fab;
            this.fabAnimSlideDown = ObjectAnimator.ofFloat(fab, "translationY", 0, screenHeight / 4).setDuration(animDuration);
            fabAnimSlideUp = ObjectAnimator.ofFloat(fab, "translationY", screenHeight / 4, 0).setDuration(animDuration);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 5 && isShowFab && (!nowAnimating)) {
                fabAnimSlideDown.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        nowAnimating = true;
                        isShowFab = false;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        fab.setVisibility(View.GONE);
                        nowAnimating = false;
                    }
                });
                fabAnimSlideDown.start();
            }
            if (dy < -5 && (!isShowFab) && (!nowAnimating)) {
                fabAnimSlideUp.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        nowAnimating = true;
                        isShowFab = true;
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        nowAnimating = false;
                    }
                });
                fabAnimSlideUp.start();
            }
        }
    }

    private void setRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(posts, this);
        recyclerView.setHasFixedSize(true); /// HERE
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.addOnScrollListener(new CustomRecyclerViewListener((FloatingActionButton) findViewById(R.id.fab), Utils.getDefaultScreenHeight()) {
        });
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
    }

    public void callInitialAuth(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void callPostCreate(View v) {
        if (receivers[POST_UPLOAD_RECEIVER] == null) {
            receivers[POST_UPLOAD_RECEIVER] = new PostUploaderReceiver(this);
        }
        newPostFragment.show();
        floatingActionButton.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        self = null;
        if (isFinishing()) {
            if (receivers[GET_INFO_RECEIVER] != null) {
                receivers[GET_INFO_RECEIVER].stop();
            }
            Loudly.getContext().stopGetInfoService();
        }
    }

    public void onPostCreated() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(newPostFragment);
        ft.commit();
        floatingActionButton.show();
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            return;
        }
        getFragmentManager().popBackStack();
    }

    static class PostUploaderReceiver extends AttachableReceiver {
        public PostUploaderReceiver(Context context) {
            super(context, Broadcasts.POST_UPLOAD);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            Toast toast;
            long postID, imageID;
            int progress;
            MainActivity mainActivity = (MainActivity) context;
            switch (status) {
                case Broadcasts.STARTED:
                    // Saved to DB. Make place for the post
                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.PROGRESS:
                    // Uploaded to network
                    postID = message.getLongExtra(Broadcasts.ID_FIELD, 0);
                    int networkID = message.getIntExtra(Broadcasts.NETWORK_FIELD, 0);
                    toast = Toast.makeText(context, "" + networkID, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Broadcasts.IMAGE:
                    // Image is loading
                    imageID = message.getLongExtra(Broadcasts.IMAGE_FIELD, 0);
                    postID = message.getLongExtra(Broadcasts.ID_FIELD, 0);
                    progress = message.getIntExtra(Broadcasts.PROGRESS_FIELD, 0);
//                    toast = Toast.makeText(context, "image " + progress, Toast.LENGTH_SHORT);
//                    toast.show();
                    Log.i("IMAGE_UPLOAD", progress + "");
                    break;
                case Broadcasts.IMAGE_FINISHED:
                    // Image loaded
                    imageID = message.getLongExtra(Broadcasts.IMAGE_FIELD, 0);
                    postID = message.getLongExtra(Broadcasts.ID_FIELD, 0);
                    networkID = message.getIntExtra(Broadcasts.NETWORK_FIELD, 0);
                    toast = Toast.makeText(context, "Image " + imageID + " uploaded to " + networkID, Toast.LENGTH_SHORT);
                    toast.show();
                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.FINISHED:
                    // LoudlyPost uploaded
                    toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                    toast.show();
                    receivers[POST_UPLOAD_RECEIVER].stop();
                    receivers[POST_UPLOAD_RECEIVER] = null;
                    mainActivity.recyclerViewAdapter.notifyItemInserted(0);
                    break;
                case Broadcasts.ERROR:
                    // Got an error
                    String errorKind = message.getStringExtra(Broadcasts.ERROR_KIND);
                    String error = message.getStringExtra(Broadcasts.ERROR_FIELD);
                    toast = Toast.makeText(context, "Fail: " + error, Toast.LENGTH_SHORT);
                    toast.show();
                    receivers[POST_UPLOAD_RECEIVER].stop();
                    receivers[POST_UPLOAD_RECEIVER] = null;
                    break;
            }
        }
    }

    static class PostLoadReceiver extends AttachableReceiver {
        public PostLoadReceiver(Context context) {
            super(context, Broadcasts.POST_LOAD);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, -1);

            Toast toast;
            MainActivity mainActivity = (MainActivity) context;
            switch (status) {
                case Broadcasts.STARTED:
                    toast = Toast.makeText(context, "DB loaded", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Broadcasts.PROGRESS:
                    toast = Toast.makeText(context,
                            "" + message.getIntExtra(Broadcasts.NETWORK_FIELD, -1), Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Broadcasts.LOADED:
                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                    ProgressBar progressBar = (ProgressBar) mainActivity.findViewById(R.id.main_activity_progress);
                    progressBar.setVisibility(View.GONE);
                    toast = Toast.makeText(context,
                            "Posts loaded", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Broadcasts.IMAGE:
                    // Here progress of loading images
                    break;
                case Broadcasts.IMAGE_FINISHED:
                    // Image loaded, show it
                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.FINISHED:

                    toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                    toast.show();
                    stop();
                    receivers[LOAD_POSTS_RECEIVER] = null;
                    loadPosts = null; // Posts loaded

                    receivers[GET_INFO_RECEIVER] = new GetInfoReceiver(context);
                    Loudly.getContext().startGetInfoService(); // Let's get info about posts
                    break;
                case Broadcasts.ERROR:
                    String error = message.getStringExtra(Broadcasts.ERROR_FIELD);
                    toast = Toast.makeText(context, "Fail: " + error, Toast.LENGTH_SHORT);
                    toast.show();
                    stop();
                    receivers[LOAD_POSTS_RECEIVER] = null;
                    break;
            }
        }
    }

    static class GetInfoReceiver extends AttachableReceiver {
        public GetInfoReceiver(Context context) {
            super(context, Broadcasts.POST_GET_INFO);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            Toast t;
            switch (status) {
                case Broadcasts.PROGRESS:
                    int networkID = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    t = Toast.makeText(context, "Got info for " + networkID, Toast.LENGTH_SHORT);
                    t.show();
                    break;
                case Broadcasts.FINISHED:
                    t = Toast.makeText(context, "Info got", Toast.LENGTH_SHORT);
                    t.show();
                    break;
                case Broadcasts.ERROR:
                    String error = message.getStringExtra(Broadcasts.ERROR_FIELD);
                    t = Toast.makeText(context, "Fail: " + error, Toast.LENGTH_SHORT);
                    t.show();
                    stop();
                    receivers[GET_INFO_RECEIVER] = null;
                    break;
            }
        }
    }

    static class PostDeleteReceiver extends AttachableReceiver {
        public PostDeleteReceiver(Context context) {
            super(context, Broadcasts.POST_DELETE);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            Toast toast;
            int id;
            switch (status) {
                case Broadcasts.PROGRESS:
                    id = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    toast = Toast.makeText(context, "Deleted: " + id, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Broadcasts.FINISHED:
                    toast = Toast.makeText(context, "Deleted from all", Toast.LENGTH_SHORT);
                    toast.show();
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                    break;
                case Broadcasts.ERROR:
                    toast = Toast.makeText(context, "Error!!!", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
            }
        }
    }
}
