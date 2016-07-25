package ly.loud.loudly.ui.brand_new;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.hannesdorfmann.mosby.mvp.viewstate.MvpViewStateActivity;
import com.hannesdorfmann.mosby.mvp.viewstate.ViewState;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.GetterModel;
import ly.loud.loudly.base.says.Post;

public class FeedActivity extends MvpViewStateActivity<FeedView, FeedPresenter>
        implements FeedView, NavigationView.OnNavigationItemSelectedListener {

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

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.nav_view)
    @NonNull
    NavigationView navigationView;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    Loudly loudlyApp;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    GetterModel getterModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Loudly.getContext().getAppComponent().inject(this);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @NonNull
    @Override
    public FeedPresenter createPresenter() {
        return new FeedPresenter(
                loudlyApp,
                getterModel
        );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showLoadedPosts(@NonNull List<Post> posts) {
        Log.e("WORKED", "YEA");
    }

    @Override
    @NonNull
    public ViewState<FeedView> createViewState() {
        return new FeedViewState();
    }

    @Override
    public void onNewViewStateInstance() {
        presenter.loadPosts();
    }

    @OnClick(R.id.fab)
    public void onNewPostClicked() {
        Snackbar.make(floatingActionButton, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
