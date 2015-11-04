package ly.loud.loudly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthActivity extends AppCompatActivity {
    WebView webView;

    private static final String TAG = "AUTH_TAG";
    private static final String ACCESS_TOKEN = "#access_token=";
    private static final String ERROR_TOKEN = "#error=";
    private static final String RESULT = "RESULT";
    private static final String TARGET = "TARGET";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        this.webView = (WebView)this.findViewById(R.id.webView);
        webView.loadUrl();
        String target = getIntent().getStringExtra(TARGET);
        switch (target) {
            case "VK":
                webView.setWebViewClient(getVKClient());
                break;
        }
    }

    private WebViewClient getVKClient() {
        return new WebViewClient() {
            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains(ACCESS_TOKEN)) {
                    int left = url.indexOf(ACCESS_TOKEN);
                    int right = left;
                    while (url.charAt(right) != '&') {
                        right++;
                    }

                    Log.d(TAG, url);
                    Log.d(TAG, url.substring(left + ACCESS_TOKEN.length(), right));

                    Intent intent = getIntent();
                    intent.putExtra(RESULT, url.substring(left + ACCESS_TOKEN.length(), right));
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (url.contains(ERROR_TOKEN)){
                    int left = url.indexOf(ERROR_TOKEN);
                    int right = left;
                    while (url.charAt(right) != '&') {
                        right++;
                    }

                    Log.d(TAG, url);
                    Log.d(TAG, url.substring(left + ERROR_TOKEN.length(), right));

                    Intent intent = getIntent();
                    intent.putExtra(RESULT, url.substring(left + ERROR_TOKEN.length(), right));
                    setResult(RESULT_CANCELED, intent);
                    finish();
                } else {
                    Log.d(TAG, "WTF?");

                    Intent intent = getIntent();
                    intent.putExtra(RESULT, "Unknown error");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            }
        };
    }
}
