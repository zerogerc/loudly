package ly.loud.loudly;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import Facebook.FacebookAuthorizer;
import Facebook.FacebookWrap;
import MailRu.MailRuAuthoriser;
import MailRu.MailRuWrap;
import VK.VKAuthorizer;
import VK.VKWrap;
import util.ContextHolder;
import util.ListenerHolder;
import base.ResponseListener;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    private String result;
    private RadioButton VKRadio;
    private RadioButton MailRadio;
    private RadioButton FacebookRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VKRadio = (RadioButton)findViewById(R.id.VKRadio);
        MailRadio = (RadioButton)findViewById(R.id.MailRuRadio);
        FacebookRadio = (RadioButton)findViewById(R.id.FacebookRadio);
    }

    public void login(View v) {
        if (VKRadio.isChecked()) {
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
        if (MailRadio.isChecked()) {
            ContextHolder.setContext(this);
            ListenerHolder.setListener(new ResponseListener() {
                @Override
                public void onSuccess(Object result1) {
                    result = "a";
                    MailRuWrap wrap = (MailRuWrap) result1;
                    Log.e(TAG, wrap.getKeys().getSessionKey());
                }

                @Override
                public void onFail(String error) {
                    result = "b";
                    Log.e(TAG, error);
                }
            });
            AsyncTask MailRuAuth = new MailRuAuthoriser().createAsyncTask();
            MailRuAuth.execute();
        }
        if (FacebookRadio.isChecked()) {
            ContextHolder.setContext(this);
            ListenerHolder.setListener(new ResponseListener() {
                @Override
                public void onSuccess(Object res) {
                    result = "a";
                    FacebookWrap wrap = (FacebookWrap) res;
                    Log.e(TAG, wrap.getKeys().getAccessToken());
                }

                @Override
                public void onFail(String error) {
                    result = "b";
                    Log.e(TAG, error);
                }
            });
            AsyncTask FacebookAuth = new FacebookAuthorizer().createAsyncTask();
            FacebookAuth.execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
