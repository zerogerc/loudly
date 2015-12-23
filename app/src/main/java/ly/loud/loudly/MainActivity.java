package ly.loud.loudly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedList;

import base.Networks;
import base.Tasks;
import base.Wrap;
import base.says.Post;
import util.AttachableReceiver;
import util.Broadcasts;
import util.UIAction;
import util.Utils;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    static LinkedList<Post> posts = new LinkedList<>();
    static boolean keysLoaded = false;
    static boolean[] loadedNetworks = new boolean[Networks.NETWORK_COUNT];
    static boolean dbLoaded = false;
    static int aliveCopy = 0;

    RecyclerView recyclerView;
    public RecyclerViewAdapter recyclerViewAdapter;
    public FloatingActionButton floatingActionButton;

    private Toolbar toolbar;

    private FrameLayout background;

    static final int LOAD_POSTS_RECEIVER = 0;
    static final int POST_UPLOAD_RECEIVER = 1;
    static final int POST_DELETE_RECEIVER = 2;
    static final int RECEIVER_COUNT = 4;

    static AttachableReceiver[] receivers = null;
    static Tasks.LoadPostsTask loadPosts = null;

    private static MainActivity self;

    public static void executeOnUI(final UIAction action) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main_call_settings) {
            callSettingsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish() {
        super.finish();
        Utils.hidePhoneKeyboard(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setShowHideAnimationEnabled(true);

        final AppBarLayout.LayoutParams customParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        background = ((FrameLayout) findViewById(R.id.main_background));
        background.setAlpha(0);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int count = getFragmentManager().getBackStackEntryCount();
                if (count > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getSupportActionBar().hide();
                    }
                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                    floatingActionButton.hide();
                    background.setAlpha(1);
                    background.getBackground().setAlpha(100);
                    background.setClickable(true);
                }
                if (count == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getSupportActionBar().show();
                    }
                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                    params.setScrollFlags(customParams.getScrollFlags());
                    floatingActionButton.show();
                    background.setAlpha(0);
                    background.setClickable(false);
                }
            }
        });

        SplashFragment.showSplash(this);

        setRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        self = this;

        if (receivers == null) {
            receivers = new AttachableReceiver[RECEIVER_COUNT];
        } else {
            for (AttachableReceiver receiver : receivers) {
                if (receiver != null) {
                    receiver.attach(this);
                }
            }
        }
        if (keysLoaded && loadPosts == null) {
            loadPosts();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        aliveCopy++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.hidePhoneKeyboard(this);
        aliveCopy--;
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            return;
        }
        if (count == 1) {
            background.setClickable(false);
            background.getBackground().setAlpha(0);
        }
        getFragmentManager().popBackStack();
    }

    static void loadPosts() {
        ArrayList<Wrap> loadFrom = new ArrayList<>();
        for (Wrap w : Loudly.getContext().getWraps()) {
            if (!loadedNetworks[w.networkID()]) {
                loadFrom.add(w);
            }
        }
        // Loading posts
        if (loadFrom.size() > 0 || !dbLoaded) {
            receivers[LOAD_POSTS_RECEIVER] = new LoadPostsReceiver(self);
            loadPosts = new Tasks.LoadPostsTask(posts, Loudly.getContext().getTimeInterval(),
                    loadFrom.toArray(new Wrap[loadFrom.size()]));
            loadPosts.execute();
        } else {
            Loudly.getContext().startGetInfoService();
        }
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
            this.fabAnimSlideUp = ObjectAnimator.ofFloat(fab, "translationY", screenHeight / 4, 0).setDuration(animDuration);
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
        recyclerView.addOnScrollListener(
                new CustomRecyclerViewListener((FloatingActionButton) findViewById(R.id.fab), Utils.getDefaultScreenHeight()) {
        });
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
    }

    public void callSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void callPostCreate(View v) {
        if (receivers[POST_UPLOAD_RECEIVER] == null) {
            receivers[POST_UPLOAD_RECEIVER] = new PostUploaderReceiver(this);
        }
        PostCreateFragment.showPostCreate(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        self = null;
        for (int i = 0; i < RECEIVER_COUNT; i++) {
            if (receivers[i] != null) {
                receivers[i].detach();
            }
        }
    }


    static class PostUploaderReceiver extends AttachableReceiver {
        public PostUploaderReceiver(Context context) {
            super(context, Broadcasts.POST_UPLOAD);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            long postID, imageID;
            int progress;
            MainActivity mainActivity = (MainActivity) context;
            switch (status) {
                case Broadcasts.STARTED:
                    // Saved to DB. Make place for the post
                    mainActivity.recyclerViewAdapter.notifyItemInserted(0);
                    Loudly.getContext().stopGetInfoService();
                    break;
                case Broadcasts.PROGRESS:
                    // Uploaded to network
                    int networkID = message.getIntExtra(Broadcasts.NETWORK_FIELD, 0);
                    String msg = "Uploading post to " + Networks.nameOfNetwork(networkID) + "...";
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            msg, Snackbar.LENGTH_INDEFINITE)
                            .show();
                    break;
                case Broadcasts.IMAGE:
                    // Image is loading
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
                    break;
                case Broadcasts.FINISHED:
                    // LoudlyPost uploaded
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            "Successfully uploaded",
                            Snackbar.LENGTH_LONG)
                            .show();

                    Loudly.getContext().startGetInfoService();
                    receivers[POST_UPLOAD_RECEIVER].stop();
                    receivers[POST_UPLOAD_RECEIVER] = null;
                    mainActivity.floatingActionButton.show();
                    break;
                case Broadcasts.ERROR:
                    // Got an error
                    int errorKind = message.getIntExtra(Broadcasts.ERROR_KIND, -1);
                    int network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    String error = "Can't upload post to " + Networks.nameOfNetwork(network) + ": ";
                    switch (errorKind) {
                        case Broadcasts.NETWORK_ERROR:
                            error += "no internet connection";
                            break;
                        case Broadcasts.INVALID_TOKEN:
                            error += "lost connection to network";
                            break;
                        default:
                            // Database fail
                            error = "Can't upload post due to internal error";
                            Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                                    error, Snackbar.LENGTH_SHORT)
                                    .show();
                            mainActivity.floatingActionButton.show();
                            Log.e("UPLOAD_POST", message.getStringExtra(Broadcasts.ERROR_FIELD));
                            stop();
                            receivers[POST_UPLOAD_RECEIVER] = null;
                            return;
                    }
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }

    static class LoadPostsReceiver extends AttachableReceiver {
        public LoadPostsReceiver(Context context) {
            super(context, Broadcasts.POST_LOAD);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, -1);

            MainActivity mainActivity = (MainActivity) context;
            int network;
            switch (status) {
                case Broadcasts.STARTED:
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            "Loading Loudly posts...",
                            Snackbar.LENGTH_INDEFINITE)
                            .show();
                    break;
                case Broadcasts.LOADED:
                    dbLoaded = true;
                    if (posts.size() == 0) {
                        Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                                "You haven't upload any post yet",
                                Snackbar.LENGTH_SHORT)
                                .show();
                        stop();
                        receivers[LOAD_POSTS_RECEIVER] = null;
                        loadPosts = null;
                    } else {
                        Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                                "Loudly posts loaded",
                                Snackbar.LENGTH_INDEFINITE)
                                .show();
                    }
                    break;
                case Broadcasts.PROGRESS:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);

                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            "Loading posts from " + Networks.nameOfNetwork(network) + "...",
                            Snackbar.LENGTH_INDEFINITE)
                            .show();
                    loadedNetworks[network] = true;
                    break;
                case Broadcasts.FINISHED:
                    if (!posts.isEmpty()) {
                        Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                                "Loaded", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    stop();
                    receivers[LOAD_POSTS_RECEIVER] = null;
                    loadPosts = null; // Posts loaded

                    Loudly.getContext().startGetInfoService(); // Let's get info about posts
                    break;
                case Broadcasts.ERROR:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    String error = "Can't load posts from " + Networks.nameOfNetwork(network) + ": ";
                    switch (message.getIntExtra(Broadcasts.ERROR_KIND, -1)) {
                        case Broadcasts.NETWORK_ERROR:
                            error += "no internet connection";
                            break;
                        case Broadcasts.INVALID_TOKEN:
                            error += "lost connection to it";
                            break;
                        default:
                            error = "Unexpected error";
                            break;
                    }
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
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
            MainActivity mainActivity = (MainActivity) context;
            int network;
            switch (status) {
                case Broadcasts.PROGRESS:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            "Deleting from " + Networks.nameOfNetwork(network) + "...", Snackbar.LENGTH_INDEFINITE)
                            .show();
                    break;
                case Broadcasts.FINISHED:
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            "Post deleted", Snackbar.LENGTH_SHORT)
                            .show();
                    mainActivity.floatingActionButton.show();
                    Loudly.getContext().startGetInfoService();
                    break;
                case Broadcasts.ERROR:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    String error = "Can't delete post from " + Networks.nameOfNetwork(network) + ": ";
                    switch (message.getIntExtra(Broadcasts.ERROR_KIND, -1)) {
                        case Broadcasts.NETWORK_ERROR:
                            error += "no internet connection";
                            break;
                        case Broadcasts.INVALID_TOKEN:
                            error += "lost connection to network";
                            break;
                        default:
                            //Totally unexpected
                            error = "Can't delete post due to internal error :(";
                            Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                                    error, Snackbar.LENGTH_SHORT)
                                    .show();
                            mainActivity.floatingActionButton.show();
                            Log.e("DELETE_POST", message.getStringExtra(Broadcasts.ERROR_FIELD));
                            stop();
                            receivers[POST_DELETE_RECEIVER] = null;
                            return;
                    }
                    Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }
}
