package VK;

import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class VKWebClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Intent intent = new Intent();
    }
}
