package ly.loud.loudly;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import base.Authorizer;
import base.KeyKeeper;
import base.Networks;
import base.Tasks;
import base.says.LoudlyPost;
import base.says.Post;
import util.AttachableReceiver;
import util.Broadcasts;
import util.UIAction;
import util.database.DatabaseActions;
import util.database.DatabaseException;

public class SettingsActivity extends AppCompatActivity {
    private static AttachableReceiver authReceiver = null;
    private IconsHolder iconsHolder;

    private AuthFragment webViewFragment;
    private View webViewFragmentView;
    
    public static String webViewURL;
    public static Authorizer webViewAuthorizer;
    public static KeyKeeper webViewKeyKeeper;

    public void setIconsClick() {
        UIAction grayItemClick = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                //TODO remove when all networks will have been implemented
                if (network == Networks.VK || network == Networks.FB) {
                    startReceiver();
                    Authorizer authorizer = Authorizer.getAuthorizer(network);
                    authorizer.createAsyncTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        };
        iconsHolder.setGrayItemClick(grayItemClick);


        UIAction colorItemClick = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                LogoutClick(((int) params[0]));
                iconsHolder.setInvisible(((int) params[0]));
            }
        };
        iconsHolder.setColorItemsClick(colorItemClick);
    }

    private void initFragment() {
        FragmentManager manager = getFragmentManager();
        webViewFragment = ((AuthFragment) manager.findFragmentById(R.id.setting_web_view));

        webViewFragmentView = findViewById(R.id.setting_web_view);
        webViewFragmentView.getBackground().setAlpha(100);
        FragmentTransaction ft = manager.beginTransaction();
        ft.hide(webViewFragment);
        ft.commit();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        iconsHolder = (IconsHolder)findViewById(R.id.settings_icons_holder);
        setIconsClick();

        initFragment();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int count = getFragmentManager().getBackStackEntryCount();
                if (count > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getSupportActionBar().hide();
                    }
                }
                if (count == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getSupportActionBar().show();
                    }
                }
            }
        });

        if (authReceiver != null) {
            authReceiver.attach(this);
        }
    }

    private static class AuthReceiver extends AttachableReceiver {
        private IconsHolder iconsHolder;
        private SettingsActivity activity;

        public AuthReceiver(SettingsActivity context, IconsHolder iconHolder) {
            super(context, Broadcasts.AUTHORIZATION);
            this.iconsHolder = iconHolder;
            this.activity = ((SettingsActivity) context);
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, 0);
            final SettingsActivity activity = (SettingsActivity)context;
            Toast toast;
            switch (status) {
                case Broadcasts.FINISHED:
                    toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                    toast.show();
                    int network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    iconsHolder.setVisible(network);
                    activity.finishWebView();
                    break;
                case Broadcasts.ERROR:
                    String error = message.getStringExtra(Broadcasts.ERROR_FIELD);
                    network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
                    activity.finishWebView();
                    toast = Toast.makeText(context, "Fail: " + error, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
            }
            stop();
            authReceiver = null;
        }
    }

    // That's here because of 3 different click listeners
    private void startReceiver() {
        authReceiver = new AuthReceiver(this, iconsHolder);
    }

    // ToDo: make buttons onclickable during authorization

    public void startWebView() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.show(webViewFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void finishWebView() {
        webViewFragment.clearWebView();
        getFragmentManager().popBackStack();
    }

    public void LogoutClick(final int network) {
        Loudly.getContext().stopGetInfoService();
        if (MainActivity.loadPosts != null) {
            MainActivity.loadPosts.stop();
        }
        AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                if (Loudly.getContext().getKeyKeeper(network) != null) {
                    try {
                        DatabaseActions.deleteKey(network);
                        Loudly.getContext().setKeyKeeper(network, null);
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                // Clean posts from this network
                for (int i = 0; i < MainActivity.posts.size(); i++) {
                    Post post = MainActivity.posts.get(i);
                    if (post.existsIn(network)) {
                        if (post instanceof LoudlyPost) {
                            boolean visible = false;
                            for (int j = 0; j < Networks.NETWORK_COUNT; j++) {
                                if (post.existsIn(j)) {
                                    visible = true;
                                    break;
                                }
                            }
                            if (!visible) {
                                MainActivity.posts.remove(i);
                                final int fixed = i;
                                i -= 1;
                                MainActivity.executeOnMain(new UIAction() {
                                    @Override
                                    public void execute(Context context, Object... params) {
                                        ((MainActivity) context).recyclerViewAdapter.notifyDeletedAtPosition(fixed);
                                    }
                                });
                            }
                        } else {
                            final int fixed = i;
                            MainActivity.posts.remove(i);
                            i -= 1;
                            MainActivity.executeOnMain(new UIAction() {
                                @Override
                                public void execute(Context context, Object... params) {
                                    ((MainActivity) context).recyclerViewAdapter.notifyDeletedAtPosition(fixed);
                                }
                            });
                        }
                    }
                }
                MainActivity.loadedNetworks[network] = false;
                Toast toast = Toast.makeText(Loudly.getContext(), "Deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    protected void onStop() {
        super.onStop();

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

    /**
     * Save keys for further use
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            if (authReceiver != null) {
                authReceiver.stop();
            }
            authReceiver = null;
            Tasks.SaveKeysTask task = new Tasks.SaveKeysTask();
            task.execute();
        }
    }
}
