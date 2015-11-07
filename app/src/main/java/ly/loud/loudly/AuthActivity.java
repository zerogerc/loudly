package ly.loud.loudly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import base.Action;
import base.Authorizer;
import base.KeyKeeper;

public class AuthActivity extends AppCompatActivity {
    ProgressBar circle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        WebView webView = (WebView)findViewById(R.id.webView);
        circle = (ProgressBar)findViewById(R.id.progressBar);
        circle.setVisibility(View.VISIBLE);
        Intent parent = getIntent();

        String url = parent.getStringExtra("URL");
        final Authorizer authorizer = parent.getParcelableExtra("AUTHORIZER");
        final KeyKeeper keys = parent.getParcelableExtra("KEYS");

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                circle.setVisibility(View.INVISIBLE);
            }

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
                    finish();
                }
            }
        });
    }
}
