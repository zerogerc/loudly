package ly.loud.loudly;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import base.Action;
import base.Authorizer;
import base.KeyKeeper;
import util.ListenerHolder;

public class AuthActivity extends AppCompatActivity {
    ProgressBar circle;
    boolean gotResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ToDo: Extract gotResponse from bundle

        gotResponse = false;

        setContentView(R.layout.activity_auth);
        final WebView webView = (WebView) findViewById(R.id.webView);
        circle = (ProgressBar) findViewById(R.id.progressBar);
        Intent parent = getIntent();

        String url = parent.getStringExtra("URL");
        final Authorizer authorizer = parent.getParcelableExtra("AUTHORIZER");
        final KeyKeeper keys = parent.getParcelableExtra("KEYS");
        webView.setVisibility(View.INVISIBLE);
        circle.setVisibility(View.VISIBLE);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (authorizer.isResponse(url)) {
                    view.setVisibility(View.INVISIBLE);
                    final String copy = url;
                    AsyncTask<Object, Void, Action> continueAuth = new AsyncTask<Object, Void, Action>() {
                        @Override
                        protected Action doInBackground(Object... params) {
                            return authorizer.continueAuthorization(copy, keys);
                        }

                        @Override
                        protected void onPostExecute(Action action) {
                            super.onPostExecute(action);
                            action.execute();
                        }
                    };
                    continueAuth.execute();
                    gotResponse = true;
                    finish();
                } else {
                    circle.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (!gotResponse) {
            Authorizer authorizer = getIntent().getParcelableExtra("AUTHORIZER");
            ListenerHolder.getListener(authorizer.network()).onFail("User declined authorisation");
        }
        super.onDestroy();

    }
}
