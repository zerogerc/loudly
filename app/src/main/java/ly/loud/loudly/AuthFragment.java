package ly.loud.loudly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import base.Authorizer;
import base.KeyKeeper;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.Utils;

public class AuthFragment extends Fragment {
    private View rootView;
    private SettingsActivity activity;
    ProgressBar circle;
    boolean gotResponse;
    WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ToDo: Extract gotResponse from bundle

        gotResponse = false;

        rootView = inflater.inflate(R.layout.activity_auth, container, false);
        activity = (SettingsActivity)getActivity();

        webView = (WebView) rootView.findViewById(R.id.webView);
        circle = (ProgressBar) rootView.findViewById(R.id.progressBar);


        return rootView;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Utils.hidePhoneKeyboard(getActivity());
            if (!gotResponse) {
                Loudly.sendLocalBroadcast(
                        BroadcastSendingTask.makeError(Broadcasts.AUTHORIZATION,
                                Broadcasts.AUTH_FAIL, "User declined authorization")
                );
            }
        } else {
            String url = SettingsActivity.webViewURL;
            final Authorizer authorizer = SettingsActivity.webViewAuthorizer;
            final KeyKeeper keys = SettingsActivity.webViewKeyKeeper;
//        webView.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);

            final Fragment fragment = this;
            webView.loadUrl(url);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i("AUTH_FRAGMENT", url);
                    if (authorizer.isResponse(url)) {
                        view.setVisibility(View.VISIBLE);

                        FinishAuthorization continueAuth = new FinishAuthorization();
                        continueAuth.execute(authorizer, url, keys);
                        gotResponse = true;

                        //TODO strange

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.hide(fragment);
                        ft.commit();
                        getFragmentManager().popBackStack();
                        return true;

                    }
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                        circle.setVisibility(View.INVISIBLE);
                        webView.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    public void clearWebView() {
        this.webView.loadUrl("about:blank");
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
}
