package ly.loud.loudly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import base.Authorizer;
import base.KeyKeeper;
import base.Networks;
import base.Tasks;
import util.AttachableReceiver;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.UIAction;

public class SplashFragment extends Fragment {
    private View rootView;
    private ImageView image;
    private TextView splashInfo;
    private WebView hiddenWebView;

    private int refreshIndex;
    private ArrayList<Integer> refreshTokens;
    private Authorizer currentAuthorizer;
    private KeyKeeper currentKeys;

    private UIAction refreshToken;

    private AttachableReceiver loadKeysReceiver;
    private AttachableReceiver tokenRefreshReciever;

    private boolean noKeys, expiredKeys;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.splash_fragment, container, false);
        image = ((ImageView) rootView.findViewById(R.id.splash_logo));
        splashInfo = (TextView) rootView.findViewById(R.id.splash_info);
        hiddenWebView = (WebView) rootView.findViewById(R.id.splash_web_view);

        hiddenWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (currentAuthorizer.isResponse(url)) {
                    FinishAuthorization continueAuth = new FinishAuthorization();
                    continueAuth.execute(currentAuthorizer, url, currentKeys);
                    return true;
                }
                return false;
            }
        });
        return rootView;
    }

    private void loadURL() {
        hiddenWebView.loadUrl(currentAuthorizer.makeAuthQuery().toURL());
    }

    private static class FinishAuthorization extends BroadcastSendingTask {
        @Override
        protected Intent doInBackground(Object... params) {
            Authorizer authorizer = (Authorizer) params[0];
            String url = (String) params[1];
            KeyKeeper keys = (KeyKeeper) params[2];

            return authorizer.continueAuthorization(url, keys);
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hide();
    }

    public void show() {
        noKeys = true;
        expiredKeys = false;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (Loudly.getContext().getKeyKeeper(i) != null) {
                noKeys = false;
                if (Loudly.getContext().getKeyKeeper(i).mayExpire()) {
                    expiredKeys = true;
                }
            }
        }
        if (noKeys || expiredKeys) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            ft.show(this);
            ft.commit();
            run();
        }
    }

    public void hide() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(this);
        ft.commit();
    }

    private void run() {
        if (noKeys) {
            loadKeysReceiver = new LoadKeysReceiver(Loudly.getContext(), this);
            new Tasks.LoadKeysTask().execute();
        } else {
            refreshTokens(this);
        }
    }

    private void refreshTokens(final SplashFragment fragment) {
        fragment.refreshTokens = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (Loudly.getContext().getKeyKeeper(i) != null) {
                KeyKeeper keys = Loudly.getContext().getKeyKeeper(i);
                if (keys.mayExpire()) {
                    fragment.refreshTokens.add(i);
                }
            }
        }
        if (fragment.refreshTokens.size() == 0) {
            fragment.hide();
            MainActivity.loadPosts();
            return;
        }

        fragment.refreshIndex = 0;
        int network = fragment.refreshTokens.get(0);
        fragment.splashInfo.setText("Login into " + Networks.nameOfNetwork(network));
        fragment.refreshToken = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                fragment.currentAuthorizer = (Authorizer) params[0];
                fragment.currentKeys = (KeyKeeper) params[1];
                fragment.loadURL();
            }
        };

        fragment.tokenRefreshReciever = new TokenRefreshReceiver(Loudly.getContext(), fragment);
        Networks.makeAuthorizer(network).createAsyncTask(Loudly.getContext(), fragment.refreshToken)
                .execute();
        fragment.refreshIndex++;
    }

    private static class LoadKeysReceiver extends AttachableReceiver {
        SplashFragment fragment;

        public LoadKeysReceiver(Context context, SplashFragment fragment) {
            super(context, Broadcasts.KEYS_LOADED);
            this.fragment = fragment;
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, -1);
            switch (status) {
                case Broadcasts.ERROR:
                    fragment.splashInfo.setText("Internal error. Please, clean application's data");
                    return;
                case Broadcasts.FINISHED:
                    stop();
                    MainActivity.keysLoaded = true;
                    fragment.loadKeysReceiver = null;
                    fragment.splashInfo.setText("Loaded");
                    fragment.refreshTokens(fragment);
            }
        }
    }

    private static class TokenRefreshReceiver extends AttachableReceiver {
        SplashFragment fragment;

        public TokenRefreshReceiver(Context context, SplashFragment fragment) {
            super(context, Broadcasts.AUTHORIZATION);
            this.fragment = fragment;
        }

        @Override
        public void onMessageReceive(Context context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, -1);
            int network = fragment.refreshTokens.get(fragment.refreshIndex);
            switch (status) {
                case Broadcasts.FINISHED:

                    if (fragment.refreshIndex == fragment.refreshTokens.size()) {
                        fragment.hide();
                        MainActivity.loadPosts();
                    }
                    fragment.splashInfo.setText("Login into " + Networks.nameOfNetwork(network));

                    Networks.makeAuthorizer(network).createAsyncTask(context, fragment.refreshToken);
                    fragment.refreshIndex++;
                    break;
                case Broadcasts.AUTH_FAIL:
                    fragment.splashInfo.setText("Can't login to " + Networks.nameOfNetwork(network));
                    fragment.hide();
                    break;
            }
            stop();
            fragment.tokenRefreshReciever = null;
        }
    }
}
