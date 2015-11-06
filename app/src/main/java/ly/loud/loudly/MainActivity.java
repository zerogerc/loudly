package ly.loud.loudly;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import VK.VKAuthorizer;
import VK.VKWrap;
import base.ContextHolder;
import base.ListenerHolder;
import base.ResponseListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    private String result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void authVKCall(View v) {
        ContextHolder.setContext(this);
        ListenerHolder.setListener(new ResponseListener() {
            @Override
            public void onSuccess(Object result1) {
                result = "a";
                VKWrap wrap = (VKWrap) result1;
                Log.e(TAG, wrap.getKeys().getAccessToken());
            }

            @Override
            public void onFail(String error) {
                result = "b";
                Log.e(TAG, error);
            }
        });
        AsyncTask VKAuth = new VKAuthorizer().createAsyncTask();
        VKAuth.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
