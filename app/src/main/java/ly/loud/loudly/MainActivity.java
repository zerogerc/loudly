package ly.loud.loudly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import util.AttachableReceiver;
import util.BroadcastSendingTask;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    private static final int LOAD_POSTS_RECEIVER = 0;
    private static final int POST_UPLOAD_RECEIVER = 1;
    private static final int POST_PROGRESS_RECEIVER = 2;
    private static final int POST_FINISHED_RECEIVER = 3;
    private static final int RECEIVER_COUNT = 4;

    private static AttachableReceiver[] receivers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (receivers == null) {
            receivers = new AttachableReceiver[RECEIVER_COUNT];
        }
        for (AttachableReceiver receiver : receivers) {
            if (receiver != null) {
                receiver.attach(this);
            }
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);

        if (!Loudly.getContext().postsLoaded) {
            // ToDo: show here rolling circle
            receivers[LOAD_POSTS_RECEIVER] = new AttachableReceiver(this, Loudly.LOADED_POSTS) {
                @Override
                public void onMessageReceive(Context context, Intent message) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.setRecyclerView();
                    stop();
                    receivers[LOAD_POSTS_RECEIVER] = null;
                }
            };
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

        // Start receivers with a little crutch
        if (receivers[POST_UPLOAD_RECEIVER] == null) {

            receivers[POST_UPLOAD_RECEIVER] = new AttachableReceiver(this, Loudly.POST_UPLOAD_STARTED) {
                @Override
                public void onMessageReceive(Context context, Intent message) {
                    // Stop itself
                    stop();
                    receivers[POST_UPLOAD_RECEIVER] = null;

                    // Make place for the post
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();

                    // Start progress receiver
                    receivers[POST_PROGRESS_RECEIVER] = new AttachableReceiver(context, Loudly.POST_UPLOAD_PROGRESS) {
                        @Override
                        public void onMessageReceive(Context context, Intent message) {
                            int progress = message.getIntExtra(BroadcastSendingTask.PROGRESS_FIELD, 0);
                            Toast toast = Toast.makeText(context, "" + progress, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    };

                    // Start finished receiver
                    receivers[POST_FINISHED_RECEIVER] = new AttachableReceiver(context, Loudly.POST_UPLOAD_FINISHED) {
                        @Override
                        public void onMessageReceive(Context context, Intent message) {
                            boolean success = message.getBooleanExtra(BroadcastSendingTask.SUCCESS_FIELD, false);

                            if (success) {
                                Toast toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                String error = message.getStringExtra(BroadcastSendingTask.ERROR_FIELD);
                                Toast toast = Toast.makeText(context, "Failed to create Post: " + error, Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            // Turn off receivers
                            receivers[POST_PROGRESS_RECEIVER].stop();
                            receivers[POST_PROGRESS_RECEIVER] = null;
                            stop();
                            receivers[POST_FINISHED_RECEIVER] = null;
                        }
                    };
                }
            };
        }
        Intent intent = new Intent(this, PostCreateActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (AttachableReceiver receiver : receivers) {
            if (receiver != null) {
                receiver.detach();
            }
        }
    }

}
