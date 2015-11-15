package ly.loud.loudly;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import Facebook.FacebookAuthorizer;
import Facebook.FacebookWrap;
import MailRu.MailRuAuthoriser;
import VK.VKAuthorizer;
import VK.VKWrap;
import base.Authorizer;
import base.Tasks;
import base.Networks;
import base.Post;
import util.ListenerHolder;
import util.LongTask;
import util.ResponseListener;
import util.TaskHolder;
import util.UIAction;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MAIN";
    private EditText postView;

    private static final int[][] networkMap = new int[][]{
            {R.id.VKRadio, Networks.VK},
            {R.id.MailRuRadio, Networks.MAILRU},
            {R.id.FacebookRadio, Networks.FB}};

    private static final int[][] checkboxMap = new int[][]{
            {Networks.VK, R.id.VKReady},
            {Networks.MAILRU, R.id.MailRuReady},
            {Networks.FB, R.id.FBReady}};

    private RadioGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        group = (RadioGroup) findViewById(R.id.networks);
        postView = (EditText) findViewById(R.id.post);
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

        if (Loudly.getContext().getKeyKeeper(network) != null) {
            return;
        }

        ListenerHolder.setListener(network, new ResponseListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                MainActivity mainActivity = (MainActivity) context;
                int checkbox = mainActivity.find(checkboxMap, network);
                ((CheckBox) mainActivity.findViewById(checkbox)).setChecked(true);
            }

            @Override
            public void onFail(Context context, String error) {
                Log.e(TAG, error);
                MainActivity mainActivity = (MainActivity) context;
                int checkbox = mainActivity.find(checkboxMap, network);
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
        authorizer.createAsyncTask(this).execute();
    }

    public void saveClicked(View v) {
        Tasks.saveKeysTask task = new Tasks.saveKeysTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                Toast toast = Toast.makeText(context, "Saved", Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        task.execute();
    }

    public void loadClicked(View v) {
        Tasks.loadKeysTask task = new Tasks.loadKeysTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                Toast toast = Toast.makeText(context, "Loaded", Toast.LENGTH_SHORT);
                toast.show();
                MainActivity mainActivity = (MainActivity) context;
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    int checkbox = mainActivity.find(checkboxMap, i);
                    ((CheckBox) mainActivity.findViewById(checkbox))
                            .setChecked(Loudly.getContext().getKeyKeeper(i) != null);
                }
            }
        };
        task.execute();
    }

    public void post(View v) {
        final TextView postView = (TextView) findViewById(R.id.post);
        String post = postView.getText().toString();
        VKWrap VkWrap = new VKWrap();
        FacebookWrap FbWrap = new FacebookWrap();

        ListenerHolder.setListener(0, new ResponseListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.postView.setText("Success!");
                }
            }

            @Override
            public void onFail(Context context, String error) {
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.postView.setText("Fail :(");
                }
            }
        });
        LongTask uploader = Tasks.makePostUploader(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                Log.e(TAG, params[0].toString());
            }
        }, VkWrap, FbWrap);
        uploader.attachContext(this);
        uploader.execute(new Post(post));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TaskHolder.getAction() != null) {
            TaskHolder.getAction().execute(this);
            TaskHolder.setAction(null);
        }
        if (TaskHolder.getTask() != null) {
            TaskHolder.getTask().attachContext(this);
        }
    }
}
