package ly.loud.loudly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import util.Action;
import base.Authorizer;
import base.KeyKeeper;
import util.AttachableTask;
import util.ListenerHolder;
import util.TaskHolder;

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
                    FinishAuthorizationTask continueAuth = new FinishAuthorizationTask();
                    continueAuth.execute(authorizer, url, keys);
                    gotResponse = true;
                    finish();
                } else {
                    circle.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    static class FinishAuthorizationTask extends AttachableTask<Object, Void> {
        @Override
        protected Action doInBackground(Object... params) {
            Authorizer authorizer = (Authorizer) params[0];
            String url = (String) params[1];
            KeyKeeper keys = (KeyKeeper) params[2];

            return authorizer.continueAuthorization(url, keys);
        }
    }

    @Override
    protected void onDestroy() {
        if (!gotResponse) {
            Log.e("TAG", "User declined authorisation");
        }
        super.onDestroy();

    }
}
