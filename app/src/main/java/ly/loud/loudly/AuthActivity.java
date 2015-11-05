package ly.loud.loudly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        this.webView = (WebView)this.findViewById(R.id.webView);
        String url = getIntent().getStringExtra("AUTH_URL");
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Intent intent = getIntent();
                intent.putExtra("RESPONSE_URL", url);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
