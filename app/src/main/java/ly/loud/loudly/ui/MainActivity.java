package ly.loud.loudly.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.PostLoadModel;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.feed.FeedFragment;
import ly.loud.loudly.ui.new_post.NetworksChooseLayout;
import ly.loud.loudly.ui.new_post.NewPostFragment.NewPostFragmentInteractions;
import ly.loud.loudly.ui.settings.SettingsActivity;
import ly.loud.loudly.ui.views.ScrimCoordinatorLayout;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_SETTLING;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ly.loud.loudly.networks.Networks.nameResourceOfNetwork;
import static ly.loud.loudly.ui.new_post.NewPostFragment.NetworksProvider;

/**
 * Main activity of Loudly application. It responds for user interaction with behavior of
 * BottomSheet Fragment (for post creation), menu and {@link NavigationView}.
 */
public class MainActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener, FragmentInvoker, NetworksProvider, NewPostFragmentInteractions {

    private static final String FEED_FRAGMENT = "feed_fragment";

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.app_bar_feed_root)
    @NonNull
    View globalRootView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.app_bar_feed_background)
    @NonNull
    ScrimCoordinatorLayout background;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.app_bar_feed_new_post_layout)
    @NonNull
    View newPostFragmentView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.app_bar_feed_networks_choose_scroll)
    @NonNull
    View bottomSheetView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.toolbar)
    @NonNull
    Toolbar toolbar;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.fab)
    @NonNull
    FloatingActionButton floatingActionButton;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.drawer_layout)
    @NonNull
    DrawerLayout drawerLayout;

    @SuppressWarnings("NullableProblems")
    @BindView(R.id.network_choose_layout)
    @NonNull
    NetworksChooseLayout networksChooseLayout;

    @SuppressWarnings("NullableProblems")
    @Inject
    @NonNull
    PostLoadModel postLoadModel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private BottomSheetBehavior bottomSheetBehavior;

    @Nullable
    private Snackbar currentSnackBar;

    @NonNull
    private final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case STATE_EXPANDED:
                    showNewPostFragment();
                     break;
                default:
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            setSlideState(bottomSheet, slideOffset);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Loudly.getApplication(this).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = ((NavigationView) findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FeedFragment(), FEED_FRAGMENT)
                    .commit();
        }

        initFragmentsInteraction();
    }

    /**
     * Perform initialization iteractions between fragments.
     */
    private void initFragmentsInteraction() {
        /**
         * It seems like workaround but as soon as I have the proper solution I change this.
         */
        globalRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    globalRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    globalRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
                bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
                if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
                    setSlideState(bottomSheetView, 1);
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                floatingActionButton.show();
            } else {
                floatingActionButton.hide();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!tryHidePostCreateLayoutBasedOnBottomSheet()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Starts fragment that replaced all content currently visible on the screen.
     * Also add this fragment to back stack.
     */
    @Override
    public void startFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, fragment.getTag())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showNetworksChooseLayout() {
        bottomSheetBehavior.setState(STATE_EXPANDED);
    }

    @Override
    public List<NetworkContract> getChosenNetworks() {
        return networksChooseLayout.getChosenNetworks();
    }

    @Override
    public void onPostUploadProgress(@NonNull LoudlyPost loudlyPost) {
        final List<SinglePost> instances = loudlyPost.getNetworkInstances();
        if (!instances.isEmpty()) {
            final int network = instances.get(instances.size() - 1).getNetwork();
            @StringRes final int resource = nameResourceOfNetwork(network);
            if (currentSnackBar != null && currentSnackBar.isShown()) {
                currentSnackBar.dismiss();
            }
            currentSnackBar = Snackbar.make(
                    floatingActionButton,
                    String.format(getString(R.string.message_post_upload_one_network), getString(resource)),
                    Snackbar.LENGTH_SHORT
            );
            currentSnackBar.show();
        }
    }

    @Override
    public void onPostUploaded() {
        Snackbar.make(floatingActionButton, getString(R.string.message_post_upload_all_networks), Snackbar.LENGTH_SHORT).show();
        FeedFragment fragment = ((FeedFragment) getSupportFragmentManager().findFragmentByTag(FEED_FRAGMENT));
        if (fragment != null) {
            fragment.updatePosts();
        }
    }

    @Override
    public void onPostButtonClicked() {
        bottomSheetBehavior.setState(STATE_COLLAPSED);
        hideNewPostFragment();
    }

    @OnClick(R.id.fab)
    public void onNewPostClicked() {
        bottomSheetBehavior.setState(STATE_EXPANDED);
        showNewPostFragment();
    }

    /**
     * Run before back button super method to check if we need to hide post layout from the screen
     * or just change state of bottom sheet.
     */
    private boolean tryHidePostCreateLayoutBasedOnBottomSheet() {
        if (newPostFragmentView.getVisibility() != VISIBLE) {
            return false;
        }

        if (bottomSheetBehavior.getState() == STATE_SETTLING && newPostFragmentView.getVisibility() == VISIBLE) {
            hideNewPostFragment();
            return true;
        }

        if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
            hideNewPostFragment();
        } else {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
        return true;
    }

    /**
     * Set PostLayout to the right state based on position of BottomSheet.
     */
    private void setSlideState(@NonNull View bottomSheet, float slideOffset) {
        int allHeight = ((View) bottomSheet.getParent()).getHeight();
        int visibleHeight = ((int) (bottomSheet.getHeight() * slideOffset));

        CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams) newPostFragmentView.getLayoutParams());
        params.height = allHeight - visibleHeight;
        newPostFragmentView.setLayoutParams(params);
    }

    /**
     * Show the fragment with new post on the screen
     */
    private void showNewPostFragment() {
        /**
         * In future we could add animations here.
         */
        newPostFragmentView.setVisibility(VISIBLE);
        background.setOpacity(1);
    }

    /**
     * Hides the fragment with new post from the screen
     */
    private void hideNewPostFragment() {
        /**
         * In future we could add animations here.
         */
        newPostFragmentView.setVisibility(GONE);
        background.setOpacity(0);
    }
}
