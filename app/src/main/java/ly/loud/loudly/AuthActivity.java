package ly.loud.loudly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import base.Authorizer;
import base.KeyKeeper;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);

        Intent parent = getIntent();

        String url = parent.getStringExtra("URL");
        final Authorizer authorizer = parent.getParcelableExtra("AUTHORIZER");
        final KeyKeeper keys = parent.getParcelableExtra("KEYS");

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (authorizer.isResponse(url)) {
                    authorizer.continueAuthorization(url, keys);
                    finish();
                }
            }
        });
    }
}
