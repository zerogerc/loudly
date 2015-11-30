package ly.loud.loudly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Fragment;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import base.Tasks;
import util.AttachableReceiver;
import util.BroadcastSendingTask;
import util.UtilsBundle;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    FloatingActionButton floatingActionButton;
    View newPostFragmentView;
    Fragment newPostFragment;
    ProgressBar progressBar;

    private static final int LOAD_POSTS_RECEIVER = 0;
    private static final int LOAD_PROGRESS_RECEIVER = 1;
    private static final int LOAD_FINISHED_RECEIVER = 2;
    private static final int POST_UPLOAD_RECEIVER = 3;
    private static final int POST_PROGRESS_RECEIVER = 4;
    private static final int POST_FINISHED_RECEIVER = 5;
    private static final int INFO_GOT_RECEIVER = 6;
    private static final int RECEIVER_COUNT = 7;

    private static AttachableReceiver[] receivers = null;
    private static Tasks.LoadPostsTask loadPosts = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (receivers == null) {
            receivers = new AttachableReceiver[RECEIVER_COUNT];
        }
        for (AttachableReceiver receiver : receivers) {
            if (receiver != null) {
                receiver.attach(this);
            }
        }

        setRecyclerView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);

        FragmentManager manager = getFragmentManager();
        newPostFragment = manager.findFragmentById(R.id.new_post_fragment);
        ((PostCreateFragment) newPostFragment).setListeners();

        newPostFragmentView = findViewById(R.id.new_post_fragment);
        newPostFragmentView.getBackground().setAlpha(100);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(newPostFragment);
        ft.commit();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        progressBar = (ProgressBar)findViewById(R.id.main_activity_progress);
        progressBar.setVisibility(View.GONE);

        if (Loudly.getContext().getPosts().isEmpty() && loadPosts == null) {
            // Loading posts
            progressBar.setVisibility(View.VISIBLE);
            // ToDo: show here rolling circle
            receivers[LOAD_POSTS_RECEIVER] = new AttachableReceiver(this, Loudly.POST_LOAD_STARTED) {
                @Override
                public void onMessageReceive(Context context, Intent message) {
                    final Toast toast = Toast.makeText(context, "DB loaded", Toast.LENGTH_SHORT);
                    toast.show();
                    stop();
                    receivers[LOAD_POSTS_RECEIVER] = null;
                    receivers[LOAD_PROGRESS_RECEIVER] = new AttachableReceiver(context, Loudly.POST_LOAD_PROGRESS) {
                        @Override
                        public void onMessageReceive(Context context, Intent message) {
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                            Toast toast = Toast.makeText(context,
                                    "" + message.getIntExtra(BroadcastSendingTask.NETWORK_FIELD, -1), Toast.LENGTH_SHORT);
                            toast.show();
                            Loudly.getContext().startGetInfoService();
                        }
                    };

                    receivers[LOAD_FINISHED_RECEIVER] = new AttachableReceiver(context, Loudly.POST_LOAD_FINISHED) {
                        @Override
                        public void onMessageReceive(Context context, Intent message) {
                            boolean success = message.getBooleanExtra(BroadcastSendingTask.SUCCESS_FIELD, false);

                            if (success) {
                                Toast toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                                toast.show();

                                MainActivity mainActivity = (MainActivity) context;
                                mainActivity.recyclerViewAdapter.notifyDataSetChanged();
                            } else {
                                String error = message.getStringExtra(BroadcastSendingTask.ERROR_FIELD);
                                Toast toast = Toast.makeText(context, "Failed to load Posts: " + error, Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            progressBar.setVisibility(View.GONE);
                            // Turn off receivers
                            receivers[LOAD_PROGRESS_RECEIVER].stop();
                            receivers[LOAD_PROGRESS_RECEIVER] = null;
                            stop();
                            receivers[LOAD_FINISHED_RECEIVER] = null;
                            loadPosts = null;
                            Loudly.getContext().startGetInfoService();
                            receivers[INFO_GOT_RECEIVER] = new AttachableReceiver(context, Loudly.POST_GET_INFO_PROGRESS) {
                                @Override
                                public void onMessageReceive(Context context, Intent message) {
                                    MainActivity mainActivity = (MainActivity) context;
                                    mainActivity.recyclerViewAdapter.notifyDataSetChanged();

                                    Toast t = Toast.makeText(context, "Info got", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            };
                        }
                    };
                }
            };


            loadPosts = new Tasks.LoadPostsTask(Loudly.getContext().getTimeInterval(),
                    Loudly.getContext().getWraps());

            loadPosts.execute();
        } else {
            setRecyclerView();
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
        recyclerViewAdapter = new RecyclerViewAdapter(Loudly.getContext().getPosts());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.addOnScrollListener(new CustomRecyclerViewListener((FloatingActionButton) findViewById(R.id.fab), UtilsBundle.getDefaultScreenHeight()) {
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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.show(newPostFragment);
        ft.commit();
        floatingActionButton.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (AttachableReceiver receiver : receivers) {
            if (receiver != null) {
                receiver.detach();
            }
        }

        if (isFinishing()) {
            Loudly.getContext().stopGetInfoService();
            if (receivers[INFO_GOT_RECEIVER] != null) {
                receivers[INFO_GOT_RECEIVER].stop();
            }
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
        if (newPostFragmentView.isShown()) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(newPostFragment);
            ft.commit();
            floatingActionButton.show();
        } else {
            super.onBackPressed();
        }
    }
}
