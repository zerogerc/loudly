package ly.loud.loudly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import Facebook.FacebookAuthorizer;
import Facebook.FacebookWrap;
import MailRu.MailRuAuthoriser;
import VK.VKAuthorizer;
import VK.VKWrap;
import util.Action;
import base.Authorizer;
import base.Networks;
import base.Post;
import util.ResponseListener;
import base.Wrap;
import util.ContextHolder;
import util.ListenerHolder;
import util.TaskHolder;
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
        ListenerHolder.setListener(network, new ResponseListener() {
            @Override
            public void onSuccess(Activity activity, Object result) {
                WrapHolder.addWrap(network, (Wrap) result);
                MainActivity mainActivity = (MainActivity) activity;
                int checkbox = mainActivity.find(checkboxMap, currentRadio);
                ((CheckBox) mainActivity.findViewById(checkbox)).setChecked(true);
            }

            @Override
            public void onFail(Activity activity, String error) {
                Log.e(TAG, error);
                MainActivity mainActivity = (MainActivity) activity;
                int checkbox = mainActivity.find(checkboxMap, currentRadio);
                ((CheckBox) mainActivity.findViewById(checkbox)).setChecked(false);
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
        final TextView postView = (TextView) findViewById(R.id.post);
        String post = postView.getText().toString();
        VKWrap VkWrap = (VKWrap) WrapHolder.getWrap(Networks.VK);
        FacebookWrap FbWrap = (FacebookWrap) WrapHolder.getWrap(Networks.FB);

        ContextHolder.setContext(this);
        ListenerHolder.setListener(Networks.VK, new ResponseListener() {
            @Override
            public void onSuccess(Activity activity, Object result) {
                Log.d(TAG, result.toString());
            }

            @Override
            public void onFail(Activity activity, String error) {
                Log.e(TAG, error);
            }
        });
        ListenerHolder.setListener(Networks.FB, new ResponseListener() {
            @Override
            public void onSuccess(Activity activity, Object result) {
                Log.d(TAG, result.toString());
            }

            @Override
            public void onFail(Activity activity, String error) {
                Log.e(TAG, error);
            }
        });

        ListenerHolder.startSession(1, new Action() {
            @Override
            public void execute(Activity activity) {
                postView.setText("Successful posting");
            }
        });

        VkWrap.post(new Post(post)).execute();
        FbWrap.post(new Post(post)).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TaskHolder.getAction() != null) {
            TaskHolder.getAction().execute(this);
            TaskHolder.setAction(null);
        }
        if (TaskHolder.getTask() != null) {
            TaskHolder.getTask().attach(this);
        }
    }
}
