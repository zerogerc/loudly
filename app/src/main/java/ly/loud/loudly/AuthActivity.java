package ly.loud.loudly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AuthActivity extends AppCompatActivity {
    WebView webView;

    private static final String ACCESS_TOKEN = "#access_token=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        this.webView = (WebView)this.findViewById(R.id.webView);
        webView.loadUrl("https://oauth.vk.com/authorize?client_id=5133011&redirect_uri=https://oauth.vk.com/blank.html&display_type=mobile&response_type=token");
        webView.setWebViewClient(new WebViewClient() {
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
                    Log.d("AUTH_TAG", url);
                    Log.d("AUTH_TAG", url.substring(left + ACCESS_TOKEN.length(), right));
                    Intent intent = getIntent();
                    intent.putExtra("RESULT", "OK_OK");
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast message = Toast.makeText(getApplicationContext(), "Access error", Toast.LENGTH_SHORT);
                    message.show();
                    Intent intent = getIntent();
                    intent.putExtra("RESULT", "NOT_OK");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            }
        });
    }
}
