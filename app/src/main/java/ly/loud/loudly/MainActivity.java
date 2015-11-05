package ly.loud.loudly;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import VK.VKAuthorizer;
import VK.VKWrap;
import base.ResponseListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void authVKCall(View v) {
        ResponseListener<MainActivity, VKWrap> listener = new ResponseListener<MainActivity, VKWrap>(this) {
            @Override
            public void onSuccess(VKWrap result) {
                Log.d(TAG, result.getKeys().getAccessToken());
            }

            @Override
            public void onFail(String error) {
                Log.d(TAG, error);
            }
        };
        AsyncTask authorize = new VKAuthorizer(listener).createAsyncTask();
        authorize.execute();
    }
}
