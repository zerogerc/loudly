package ly.loud.loudly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import Facebook.FacebookAuthorizer;
import MailRu.MailRuAuthoriser;
import VK.VKAuthorizer;
import VK.VKWrap;
import base.Authorizer;
import base.Networks;
import base.Post;
import base.ResponseListener;
import base.Wrap;
import base.attachments.Text;
import util.ContextHolder;
import util.ListenerHolder;
import util.WrapHolder;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";

    private static final int[][] networkMap = new int[][]{
            {R.id.VKRadio, Networks.VK},
            {R.id.MailRuRadio, Networks.MAILRU},
            {R.id.FacebookRadio, Networks.FB}};

    private static final int[][] checkboxMap = new int[][]{
            {R.id.VKRadio, R.id.VKReady},
            {R.id.MailRuRadio, R.id.MailRuReady},
            {R.id.FacebookRadio, R.id.FBReady}};

    private RadioGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        group = (RadioGroup) findViewById(R.id.networks);
    }

    // ToDo: replace with dictionary

    int find(int[][] map, int val) {
        for (int[] aMap : map) {
            if (aMap[0] == val) {
                return aMap[1];
            }
        }
        return -1;
    }

    public void login(View v) {
        final int currentRadio = group.getCheckedRadioButtonId();

        final int network = find(networkMap, currentRadio);

        if (WrapHolder.getWrap(network) != null) {
            return;
        }

        ContextHolder.setContext(this);
        ListenerHolder.setListener(new ResponseListener() {
            @Override
            public void onSuccess(Object result) {
                WrapHolder.addWrap(network, (Wrap) result);

                int checkbox = find(checkboxMap, currentRadio);
                ((CheckBox)findViewById(checkbox)).setChecked(true);
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG, error);
                int checkbox = find(checkboxMap, currentRadio);
                ((CheckBox)findViewById(checkbox)).setChecked(false);
            }
        });

        Authorizer authorizer;
        switch (network) {
            case Networks.VK:
                authorizer = new VKAuthorizer();
                break;
            case Networks.MAILRU:
                authorizer = new MailRuAuthoriser();
                break;
            default:
                authorizer = new FacebookAuthorizer();
                break;
        }
        authorizer.createAsyncTask().execute();
    }

    public void post(View v) {
        TextView postView = (TextView) findViewById(R.id.post);
        String post = postView.getText().toString();
        VKWrap wrap = (VKWrap) WrapHolder.getWrap(Networks.VK);
        ContextHolder.setContext(this);
        ListenerHolder.setListener(new ResponseListener() {
            @Override
            public void onSuccess(Object result) {
                Log.d(TAG, result.toString());
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG, error);

            }
        });
        wrap.post(new Post(new Text("well done!"))).execute();
        postView.setText("ok");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
