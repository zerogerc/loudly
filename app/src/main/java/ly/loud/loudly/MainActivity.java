package ly.loud.loudly;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

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
    private RadioButton VKRadio;
    private RadioButton MailRadio;
    private RadioButton FacebookRadio;
    private CheckBox VKcheck;
    private CheckBox Mailcheck;
    private CheckBox FBcheck;
    private boolean VKReady, MailReady, FBReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VKRadio = (RadioButton)findViewById(R.id.VKRadio);
        MailRadio = (RadioButton)findViewById(R.id.MailRuRadio);
        FacebookRadio = (RadioButton)findViewById(R.id.FacebookRadio);
        VKcheck = (CheckBox)findViewById(R.id.VKReady);
        Mailcheck = (CheckBox)findViewById(R.id.MailRuReady);
        FBcheck = (CheckBox)findViewById(R.id.FBReady);

        if (savedInstanceState != null) {
            VKReady = savedInstanceState.getBoolean("vk");
            MailReady = savedInstanceState.getBoolean("mail");
            FBReady = savedInstanceState.getBoolean("fb");
            VKcheck.setChecked(VKReady);
            Mailcheck.setChecked(MailReady);
            FBcheck.setChecked(FBReady);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("vk", VKReady);
        outState.putBoolean("mail", MailReady);
        outState.putBoolean("fb", FBReady);
    }

    public void login(View v) {
        if (!VKReady && VKRadio.isChecked()) {
            ContextHolder.setContext(this);
            ListenerHolder.setListener(new ResponseListener() {
                @Override
                public void onSuccess(Object result1) {
                    VKReady = true;
                    VKWrap wrap = (VKWrap) result1;
                    ((CheckBox)findViewById(R.id.VKReady)).setChecked(true);
                    Log.e(TAG, wrap.getKeys().getAccessToken());
                }

                @Override
                public void onFail(String error) {
                    VKReady = false;
                    Log.e(TAG, error);
                }
            });
            AsyncTask VKAuth = new VKAuthorizer().createAsyncTask();
            VKAuth.execute();
        }
        if (!MailReady && MailRadio.isChecked()) {
            ContextHolder.setContext(this);
            ListenerHolder.setListener(new ResponseListener() {
                @Override
                public void onSuccess(Object result1) {
                    MailReady = true;
                    ((CheckBox)findViewById(R.id.MailRuReady)).setChecked(true);
                    MailRuWrap wrap = (MailRuWrap) result1;
                    Log.e(TAG, wrap.getKeys().getSessionKey());
                }

                @Override
                public void onFail(String error) {
                    MailReady = false;
                    Log.e(TAG, error);
                }
            });
            AsyncTask MailRuAuth = new MailRuAuthoriser().createAsyncTask();
            MailRuAuth.execute();
        }
        if (!FBReady && FacebookRadio.isChecked()) {
            ContextHolder.setContext(this);
            ListenerHolder.setListener(new ResponseListener() {
                @Override
                public void onSuccess(Object res) {
                    FBReady = true;
                    ((CheckBox)findViewById(R.id.FBReady)).setChecked(true);
                    FacebookWrap wrap = (FacebookWrap) res;
                    Log.e(TAG, wrap.getKeys().getAccessToken());
                }

                @Override
                public void onFail(String error) {
                    FBReady = false;
                    Log.e(TAG, error);
                }
            });
            AsyncTask FacebookAuth = new FacebookAuthorizer().createAsyncTask();
            FacebookAuth.execute();
        }
    }

    public void post(View v) {
        TextView postView = (TextView)findViewById(R.id.post);
        String post = postView.getText().toString();
        postView.setText("ok");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
