package ly.loud.loudly.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.NetworkContract;
import ly.loud.loudly.ui.feed.FeedFragment;
import ly.loud.loudly.ui.new_post.NetworksChooseLayout;
import ly.loud.loudly.ui.new_post.NewPostFragment;
import ly.loud.loudly.ui.settings.SettingsActivity;
import ly.loud.loudly.ui.views.ScrimCoordinatorLayout;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Main activity of Loudly application. It responds for user interaction with behavior of
 * BottomSheet Fragment (for post creation), menu and {@link NavigationView}.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentInvoker , NewPostFragment.NetworksProvider {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.app_bar_feed_new_post_root)
    @NonNull
    View newPostRoot;

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
    View networkChooseScroll;

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

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private BottomSheetBehavior bottomSheetBehavior;

    @NonNull
    private final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case STATE_EXPANDED:
                    background.setOpacity(1);
                    newPostRoot.setVisibility(VISIBLE);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            int allHeight = ((View) bottomSheet.getParent()).getHeight();
            int visibleHeight = ((int) (bottomSheet.getHeight() * slideOffset));

            CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams) newPostFragmentView.getLayoutParams());
            params.height = allHeight - visibleHeight;
            newPostFragmentView.setLayoutParams(params);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Loudly.getContext().getAppComponent().inject(this);

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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FeedFragment())
                .commit();

        initFragmentsInteraction();
    }

    /**
     * Perform initialization of post creation fragment.
     */
    private void initFragmentsInteraction() {
        bottomSheetBehavior = BottomSheetBehavior.from(networkChooseScroll);
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

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
        } else if (newPostRoot.getVisibility() == VISIBLE){
            hidePostCreateFragment();
        } else {
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
//            Intent intent = new Intent(this, MaterialSettingsActivity.class);
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
    public List<NetworkContract> getChosenNetworks() {
        return networksChooseLayout.getChosenNetworks();
    }

    private void hidePostCreateFragment() {
        bottomSheetBehavior.setState(STATE_COLLAPSED);
        newPostRoot.setVisibility(GONE);
        background.setOpacity(0);
    }

    @OnClick(R.id.fab)
    public void onNewPostClicked() {
        bottomSheetBehavior.setState(STATE_EXPANDED);
    }
}
