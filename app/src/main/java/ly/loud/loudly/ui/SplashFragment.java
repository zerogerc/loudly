package ly.loud.loudly.ui;

import android.app.Activity;
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

import ly.loud.loudly.base.Authorizer;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.R;
import ly.loud.loudly.util.AttachableReceiver;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.UIAction;

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
                    currentAuthorizer.createFinishAuthorizationTask(currentKeys, url)
                            .execute();
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void finishSplash() {
        getFragmentManager().popBackStack();
    }

    private static void show(Activity activity, SplashFragment fragment) {
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public static void showSplash(Activity activity) {
        SplashFragment newFragment = new SplashFragment();
        newFragment.noKeys = true;
        newFragment.expiredKeys = false;

        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (Loudly.getContext().getKeyKeeper(i) != null) {
                newFragment.noKeys = false;
                if (Loudly.getContext().getKeyKeeper(i).mayExpire()) {
                    newFragment.expiredKeys = true;
                }
            }
        }
        if (newFragment.noKeys || newFragment.expiredKeys) {
            show(activity, newFragment);
            newFragment.run();
        }
    }

    private void run() {
        if (noKeys) {
            // Load keys
            tokenRefreshReciever = new TokenRefreshReceiver(Loudly.getContext(), this);
            Networks.makeAuthorizer(Networks.LOUDLY)
                    .createAsyncTask(null, null)
                    .execute();
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
            finishSplash();
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

        Networks.makeAuthorizer(network).createAsyncTask(Loudly.getContext(), fragment.refreshToken)
                .execute();
        fragment.refreshIndex++;
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
            int network = message.getIntExtra(Broadcasts.NETWORK_FIELD, -1);
            boolean needFinish = false;
            switch (status) {
                case Broadcasts.FINISHED:
                    if (network == Networks.LOUDLY) {
                        MainActivity.keysLoaded = true;
                        fragment.loadKeysReceiver = null;
                        fragment.splashInfo.setText("Loaded");
                        fragment.refreshTokens(fragment);
                        return;
                    }
                    if (fragment.refreshIndex == fragment.refreshTokens.size()) {
                        needFinish = true;
                        MainActivity.loadPosts();
                    }
                    fragment.splashInfo.setText("Login into " + Networks.nameOfNetwork(network));

                    Networks.makeAuthorizer(network).createAsyncTask(context, fragment.refreshToken);
                    fragment.refreshIndex++;
                    break;
                case Broadcasts.AUTH_FAIL:
                    fragment.splashInfo.setText("Can't login to " + Networks.nameOfNetwork(network));
                    needFinish = true;
                    break;
            }
            stop();
            if (needFinish) {
                fragment.finishSplash();
            }
            fragment.tokenRefreshReciever = null;
        }
    }
}