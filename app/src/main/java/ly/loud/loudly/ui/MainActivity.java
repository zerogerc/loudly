package ly.loud.loudly.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.ui.brand_new.views.FeedRecyclerView;
import ly.loud.loudly.util.AttachableReceiver;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MAIN";
    private static final String BUNDLE_RECYCLER_LAYOUT = "Recycler View State";

    public static boolean[] loadedNetworks = new boolean[Networks.NETWORK_COUNT];

    @BindView(R.id.content_main_feed_view)
    FeedRecyclerView feedRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @Inject
    Handler mainThreadHandler;

    static final int LOAD_POSTS_RECEIVER = 0;
    static final int POST_UPLOAD_RECEIVER = 1;
    static final int POST_DELETE_RECEIVER = 2;
    static final int RECEIVER_COUNT = 4;

    static AttachableReceiver[] receivers = null;

    private static MainActivity self;

    public static void executeOnUI(final UIAction<MainActivity> action) {
        if (self != null) {
            self.runOnUiThread(() -> action.execute(self));
        }
    }

    private void init() {
        self = this;

        setFeedRecyclerView(new PostsAdapter(Loudly.getPostHolder().getPosts(), this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ((Loudly) getApplication()).getAppComponent().inject(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.feed);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();

        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt(BUNDLE_RECYCLER_LAYOUT);
            feedRecyclerView.getLayoutManager().scrollToPosition(position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int position;
        if (feedRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            int[] positions = new int[2];
            ((StaggeredGridLayoutManager) feedRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(positions);
            position = positions[0];
        } else {
            position = ((LinearLayoutManager) feedRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        outState.putInt(BUNDLE_RECYCLER_LAYOUT, position);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                super.onBackPressed();
                getFragmentManager().popBackStack();
                return;
            }

            if (count == 1) {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            callSettingsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        item.getTitle();
        if (id == R.id.nav_no_filter) {
            setFeedRecyclerView(new PostsAdapter(Loudly.getPostHolder().getPosts(), this));
            getSupportActionBar().setTitle(R.string.feed);
        } else if (id == R.id.nav_loudly) {
            setFeedRecyclerView(new FilteredPostsAdapter(Loudly.getPostHolder().getPosts(), Networks.LOUDLY, this));
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_facebook) {
            setFeedRecyclerView(new FilteredPostsAdapter(Loudly.getPostHolder().getPosts(), Networks.FB, this));
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_instagram) {
            setFeedRecyclerView(new FilteredPostsAdapter(Loudly.getPostHolder().getPosts(), Networks.INSTAGRAM, this));
            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.nav_vk) {
            setFeedRecyclerView(new FilteredPostsAdapter(Loudly.getPostHolder().getPosts(), Networks.VK, this));
            getSupportActionBar().setTitle(item.getTitle());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        self = this;
        Loudly.setCurrentActivity(this);

        if (receivers == null) {
            receivers = new AttachableReceiver[RECEIVER_COUNT];
        } else {
            for (AttachableReceiver receiver : receivers) {
                if (receiver != null) {
                    receiver.attach(this);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.hidePhoneKeyboard(this);
        Loudly.clearCurrentActivity(this);
    }

    static void loadPosts() {
        ArrayList<Wrap> loadFrom = new ArrayList<>();
        for (Wrap w : Loudly.getContext().getWraps()) {
            if (!loadedNetworks[w.networkID()]) {
                loadFrom.add(w);
            }
        }
        // Loading posts
        if (loadFrom.size() > 0) {
            receivers[LOAD_POSTS_RECEIVER] = new LoadPostsReceiver(self);
        } else {
            Loudly.getContext().startGetInfoService();
        }
    }

    private void setFeedRecyclerView(@NonNull PostsAdapter adapter) {
        Loudly.getPostHolder().setAdapter(adapter);
        feedRecyclerView.setAdapter(adapter);
    }

    public void callSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void callPostCreate() {
        if (receivers[POST_UPLOAD_RECEIVER] == null) {
            receivers[POST_UPLOAD_RECEIVER] = new PostUploaderReceiver(this);
        }
//        PostCreateFragment.newInstance().show(getFragmentManager(), PostCreateFragment.TAG);
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

    static class PostUploaderReceiver extends AttachableReceiver<MainActivity> {
        public PostUploaderReceiver(MainActivity context) {
            super(context, Broadcasts.POST_UPLOAD);
        }

        @Override
        public void onMessageReceive(MainActivity context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            long postID, imageID;
            int progress;
            String msg;
            switch (status) {
                case Broadcasts.STARTED:
                    // Saved to DB. Make place for the post
                    msg = "Uploading post...";
                    Snackbar.make(context.findViewById(R.id.fab),
                            msg, Snackbar.LENGTH_INDEFINITE)
                            .show();
                    break;
                case Broadcasts.PROGRESS:
                    // Uploaded to network
                    int networkID = message.getIntExtra(Broadcasts.NETWORK_FIELD, 0);
                    msg = "Post uploaded to " + Networks.nameOfNetwork(networkID) + "...";
                    Snackbar.make(context.findViewById(R.id.fab),
                            msg, Snackbar.LENGTH_SHORT)
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
                    Snackbar.make(context.findViewById(R.id.fab),
                            "Successfully uploaded",
                            Snackbar.LENGTH_LONG)
                            .show();

                    Loudly.getContext().startGetInfoService();
                    receivers[POST_UPLOAD_RECEIVER].stop();
                    receivers[POST_UPLOAD_RECEIVER] = null;
                    context.floatingActionButton.setVisibility(View.VISIBLE);
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
                        case Broadcasts.EXPIRED_TOKEN:
                            error += "lost connection to network";
                            break;
                        default:
                            // Database fail
                            error = "Can't upload post due to internal error";
                            Snackbar.make(context.findViewById(R.id.fab),
                                    error, Snackbar.LENGTH_SHORT)
                                    .show();
                            context.floatingActionButton.setVisibility(View.VISIBLE);
                            Log.e("UPLOAD_POST", message.getStringExtra(Broadcasts.ERROR_FIELD));
                            stop();
                            receivers[POST_UPLOAD_RECEIVER] = null;
                            return;
                    }
                    Snackbar.make(context.findViewById(R.id.fab),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }

    static class LoadPostsReceiver extends AttachableReceiver<MainActivity> {
        public LoadPostsReceiver(MainActivity context) {
            super(context, Broadcasts.POST_LOAD);
        }

        @Override
        public void onMessageReceive(MainActivity context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, -1);

            int network;
            switch (status) {
                case Broadcasts.STARTED:
                    Snackbar.make(context.findViewById(R.id.fab),
                            "Loading Loudly posts...",
                            Snackbar.LENGTH_INDEFINITE)
                            .show();
                    break;
                case Broadcasts.LOADED:
                    if (Loudly.getPostHolder().getPosts().isEmpty()) {
                        Snackbar.make(context.findViewById(R.id.fab),
                                "You haven't upload any post yet",
                                Snackbar.LENGTH_SHORT)
                                .show();
                        stop();
                        receivers[LOAD_POSTS_RECEIVER] = null;
                    } else {
                        Snackbar.make(context.findViewById(R.id.fab),
                                "Loudly posts loaded",
                                Snackbar.LENGTH_INDEFINITE)
                                .show();
                    }
                    break;
                case Broadcasts.PROGRESS:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);

                    Snackbar.make(context.findViewById(R.id.fab),
                            "Loading posts from " + Networks.nameOfNetwork(network) + "...",
                            Snackbar.LENGTH_INDEFINITE)
                            .show();
                    loadedNetworks[network] = true;
                    break;
                case Broadcasts.FINISHED:
                    Snackbar.make(context.findViewById(R.id.fab),
                            "Loaded", Snackbar.LENGTH_SHORT)
                            .show();
                    stop();
                    receivers[LOAD_POSTS_RECEIVER] = null;

                    Loudly.getContext().startGetInfoService(); // Let's get info about posts
                    break;
                case Broadcasts.ERROR:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    String error = "Can't load posts from " + Networks.nameOfNetwork(network) + ": ";
                    switch (message.getIntExtra(Broadcasts.ERROR_KIND, -1)) {
                        case Broadcasts.NETWORK_ERROR:
                            error += "no internet connection";
                            break;
                        case Broadcasts.EXPIRED_TOKEN:
                            error += "lost connection to it";
                            break;
                        default:
                            error = "Unexpected error";
                            break;
                    }
                    Snackbar.make(context.findViewById(R.id.fab),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
                    loadedNetworks[network] = false;
                    break;
            }
        }
    }

    static class PostDeleteReceiver extends AttachableReceiver<MainActivity> {
        public PostDeleteReceiver(MainActivity context) {
            super(context, Broadcasts.POST_DELETE);
        }

        @Override
        public void onMessageReceive(MainActivity context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            int network;
            switch (status) {
                case Broadcasts.PROGRESS:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    Snackbar.make(context.findViewById(R.id.fab),
                            "Deleting from " + Networks.nameOfNetwork(network) + "...", Snackbar.LENGTH_INDEFINITE)
                            .show();
                    break;
                case Broadcasts.FINISHED:
                    Snackbar.make(context.findViewById(R.id.fab),
                            "Post deleted", Snackbar.LENGTH_SHORT)
                            .show();
                    Loudly.getContext().startGetInfoService();
                    break;
                case Broadcasts.ERROR:
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    String error = "Can't delete post from " + Networks.nameOfNetwork(network) + ": ";
                    switch (message.getIntExtra(Broadcasts.ERROR_KIND, -1)) {
                        case Broadcasts.NETWORK_ERROR:
                            error += "no internet connection";
                            break;
                        case Broadcasts.EXPIRED_TOKEN:
                            error += "lost connection to network";
                            break;
                        default:
                            //Totally unexpected
                            error = "Can't delete post due to internal error :(";
                            Snackbar.make(context.findViewById(R.id.fab),
                                    error, Snackbar.LENGTH_SHORT)
                                    .show();
                            Log.e("DELETE_POST", message.getStringExtra(Broadcasts.ERROR_FIELD));
                            stop();
                            receivers[POST_DELETE_RECEIVER] = null;
                            return;
                    }
                    Snackbar.make(context.findViewById(R.id.fab),
                            error, Snackbar.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    }

    @OnClick(R.id.fab)
    public void fabClick() {
        callPostCreate();
    }
}
