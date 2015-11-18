package ly.loud.loudly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import base.Post;
import base.Tasks;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";

    private void populateList(List<Post> posts) {
        for (int i = 0; i < 50; i++) {
            if (i % 2 == 0) {
                Post post = new Post("Success №" + i);
                posts.add(post);
            } else {
                Post post = new Post("aadgdgadgal;dsgl;adsgl;kdasl;gkads;ka;dskvl;akdkgoqejeoitgjejgladsjgdsdas,.basdfgadsgdsgadsl;gdasjgadksjgadsvdasvjdsavadkslvjadskbdjadskbjdsbjadskbdsajlbdas");
                posts.add(post);
            }
        }
    }

    private void setRecyclerView() {
        List<Post> posts = new LinkedList<>();
        populateList(posts);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(posts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

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

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);
        setRecyclerView();
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
        Tasks.saveKeysTask task = new Tasks.saveKeysTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                Toast toast = Toast.makeText(context, "Saved", Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        task.execute();
    }

    public void savePost(View v) {
        Tasks.savePostsTask task = new Tasks.savePostsTask(this) {
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
        Tasks.loadPostsTask task = new Tasks.loadPostsTask(this) {
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
}
