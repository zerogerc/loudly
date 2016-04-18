package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import java.util.ArrayList;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Authorizer;
import ly.loud.loudly.base.KeyKeeper;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.util.AttachableReceiver;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.UIAction;

public class SplashFragment extends DialogFragment {
    public static String TAG = "Splash";

    private View rootView;
    private ImageView image;
    private WebView hiddenWebView;

    private int refreshIndex;
    private ArrayList<Integer> refreshTokens;
    private Authorizer currentAuthorizer;
    private KeyKeeper currentKeys;

    private UIAction refreshToken;

    private AttachableReceiver loadKeysReceiver;
    private AttachableReceiver tokenRefreshReciever;

    private boolean noKeys, expiredKeys;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        rootView = inflater.inflate(R.layout.splash_fragment, null);

        image = ((ImageView) rootView.findViewById(R.id.splash_logo));
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

        builder.setView(rootView);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO: need testing
        run();
    }

    private void loadURL() {
        hiddenWebView.loadUrl(currentAuthorizer.makeAuthQuery().toURL());
    }

    private void finishSplash() {
        dismiss();
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
            newFragment.show(activity.getFragmentManager(), TAG);
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
//        fragment.splashInfo.setText("Login into " + Networks.nameOfNetwork(network));
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
//                        fragment.splashInfo.setText("Loaded");
                        fragment.refreshTokens(fragment);
                        return;
                    }
                    if (fragment.refreshIndex == fragment.refreshTokens.size()) {
                        needFinish = true;
                        MainActivity.loadPosts();
                    }
//                    fragment.splashInfo.setText("Login into " + Networks.nameOfNetwork(network));

                    Networks.makeAuthorizer(network).createAsyncTask(context, fragment.refreshToken);
                    fragment.refreshIndex++;
                    break;
                case Broadcasts.AUTH_FAIL:
//                    fragment.splashInfo.setText("Can't login to " + Networks.nameOfNetwork(network));
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
