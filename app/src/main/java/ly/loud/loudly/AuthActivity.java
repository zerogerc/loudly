package ly.loud.loudly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import base.Authorizer;
import base.KeyKeeper;
import util.BroadcastSendingTask;
import util.UtilsBundle;

public class AuthActivity extends Fragment {
    private View rootView;
    private SettingsActivity activity;
    ProgressBar circle;
    boolean gotResponse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ToDo: Extract gotResponse from bundle

        gotResponse = false;

        rootView = inflater.inflate(R.layout.activity_auth, container, false);
        activity = (SettingsActivity)getActivity();

        final WebView webView = (WebView) rootView.findViewById(R.id.webView);
        circle = (ProgressBar) rootView.findViewById(R.id.progressBar);

        String url = activity.webViewURL;
        final Authorizer authorizer = activity.webViewAuthorizer;
        final KeyKeeper keys = activity.webViewKeyKeeper;
//        webView.setVisibility(View.INVISIBLE);
        circle.setVisibility(View.VISIBLE);

        final Fragment fragment = this;
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (authorizer.isResponse(url)) {
                    view.setVisibility(View.VISIBLE);

                    FinishAuthorization continueAuth = new FinishAuthorization();
                    continueAuth.execute(authorizer, url, keys);
                    gotResponse = true;

                    //TODO strange

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.hide(fragment);
                    ft.commit();

                } else {
                    circle.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });

        return rootView;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            UtilsBundle.hidePhoneKeyboard(getActivity());
            if (!gotResponse) {
                LocalBroadcastManager.getInstance(Loudly.getContext()).sendBroadcast(
                        BroadcastSendingTask.makeError(Loudly.AUTHORIZATION_FINISHED, -1, "User declined authorization")
                );
            }
        } else {
            //TODO maybe show???
        }

    }

    private static class FinishAuthorization extends BroadcastSendingTask<Object> {
        @Override
        protected Intent doInBackground(Object... params) {
            Authorizer authorizer = (Authorizer) params[0];
            String url = (String) params[1];
            KeyKeeper keys = (KeyKeeper) params[2];

            return authorizer.continueAuthorization(url, keys);
        }
    }

}
