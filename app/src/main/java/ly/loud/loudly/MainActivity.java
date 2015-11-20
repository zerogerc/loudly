package ly.loud.loudly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import base.Tasks;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    BroadcastReceiver checkLoaded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setRecyclerView();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(checkLoaded);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);

        if (!Loudly.getContext().arePostsLoaded()) {
            // ToDo: show here rolling circle
            IntentFilter filter = new IntentFilter(Loudly.LOUDLY_LOADED_POSTS);
            LocalBroadcastManager.getInstance(this).registerReceiver(checkLoaded, filter);
        } else {
            setRecyclerView();
        }
    }

    private void setRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(Loudly.getContext().getPosts());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 10 && fab.isShown()) {
                    fab.hide();
                }
                if (dy < -10 && (!fab.isShown())) {
                    fab.show();
                }
            }
        });

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
    }

    public void callInitialAuth(View v) {
        Intent intent = new Intent(this, InitialSettingsActivity.class);
        startActivity(intent);
    }

    public void callPostCreate(View v) {
        Intent intent = new Intent(this, PostCreateActivity.class);
        startActivity(intent);
    }

    // ToDo: replace with dictionary

    int find(int[][] map, int val) {
        for (int[] aMap : map) {
            if (aMap[0] == val) {
                return aMap[1];
            }
        }
        return -1;
    }

    public void saveClicked(View v) {
        Tasks.SaveKeysTask task = new Tasks.SaveKeysTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                Toast toast = Toast.makeText(context, "Saved", Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        task.execute();
    }

    public void savePost(View v) {
        Tasks.SavePostsTask task = new Tasks.SavePostsTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                if (integer == 0) {
                    Toast toast = Toast.makeText(context, "Saved", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context, "Failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };
        task.execute();
    }

    public void loadPost(View v) {
        Tasks.LoadPostsTask task = new Tasks.LoadPostsTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                if (integer == 0) {
                    Toast toast = Toast.makeText(context, "Loaded", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context, "Failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Loudly context = Loudly.getContext();
        if (context.getAction() != null) {
            context.getAction().execute(this);
            context.setAction(null);
        }
        if (context.getTask() != null) {
            context.getTask().attachContext(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
