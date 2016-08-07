package ly.loud.loudly.ui.auth;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.Authorizer;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.ui.settings.SettingsActivity;
import ly.loud.loudly.util.BroadcastSendingTask;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.Utils;

public class AuthFragment extends Fragment {
    private View rootView;
    ProgressBar circle;
    boolean gotResponse;
    WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            gotResponse = savedInstanceState.getBoolean("got");
        } else {
            gotResponse = false;
        }

        rootView = inflater.inflate(R.layout.activity_auth, container, false);

        webView = (WebView) rootView.findViewById(R.id.webView);
        circle = (ProgressBar) rootView.findViewById(R.id.progressBar);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("got", gotResponse);
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

            circle.setVisibility(View.VISIBLE);

            final Fragment fragment = this;
            webView.loadUrl(url);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i("AUTH_FRAGMENT", url);
                    if (authorizer.isResponse(url)) {
                        view.setVisibility(View.VISIBLE);

                        authorizer.createFinishAuthorizationTask(keys, url).
                                execute();
                        gotResponse = true;

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
}
