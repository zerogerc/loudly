package ly.loud.loudly;

import android.content.Context;
import android.content.Intent;
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
import base.Networks;
import base.Post;
import base.Tasks;
import util.LongTask;
import util.ResultListener;
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

        getSupportActionBar().setCustomView(R.layout.toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        group = (RadioGroup) findViewById(R.id.networks);
        postView = (EditText) findViewById(R.id.post);
    }

    public void callInitialAuth(View v) {
        Intent intent = new Intent(this, InitialSettingsActivity.class);
        startActivity(intent);
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

        Loudly.getContext().setListener(new ResultListener() {
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
                    if (checkbox == -1) {
                        continue;
                    }
                    ((CheckBox) mainActivity.findViewById(checkbox))
                            .setChecked(Loudly.getContext().getKeyKeeper(i) != null);
                }
            }
        };
        task.execute();
    }

    public void savePost(View v) {
        Tasks.savePostsTask task = new Tasks.savePostsTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                if (integer == 0) {
                    Toast toast = Toast.makeText(context, "Saved", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context, "Failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };
        task.execute();
    }

    public void loadPost(View v) {
        Tasks.loadPostsTask task = new Tasks.loadPostsTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {
                if (integer == 0) {
                    Toast toast = Toast.makeText(context, "Loaded", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context, "Failed", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };
        task.execute();
    }

    public void getInfo(View v) {
        final ResultListener onFinish = new ResultListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                MainActivity mainActivity = (MainActivity) context;
                TextView likes = (TextView) mainActivity.findViewById(R.id.likes);
                Post post = (Post) result;
                String text = Integer.toString(post.getInfo(Networks.FB).like) + " " +
                        Integer.toString(post.getInfo(Networks.VK).like);
                likes.setText(text);
            }

            @Override
            public void onFail(Context context, String error) {
                Toast toast = Toast.makeText(context, "Failed", Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        Post post = Loudly.getContext().getPosts().getLast();
        LongTask getter = Tasks.makePostInfoGetter(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                Log.e(TAG, params[0].toString());
            }
        }, onFinish, new VKWrap(), new FacebookWrap());
        getter.attachContext(this);
        getter.execute(post);
    }

    public void post(View v) {
        final TextView postView = (TextView) findViewById(R.id.post);
        String post = postView.getText().toString();
        VKWrap VkWrap = new VKWrap();
        FacebookWrap FbWrap = new FacebookWrap();

        ResultListener onFinish = new ResultListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                Toast toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onFail(Context context, String error) {
                Toast toast = Toast.makeText(context, error, Toast.LENGTH_SHORT);
                toast.show();
            }
        };

        LongTask uploader = Tasks.makePostUploader(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                Log.e(TAG, params[0].toString());
            }
        }, onFinish, VkWrap, FbWrap);
        uploader.attachContext(this);
        uploader.execute(new Post(post));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Loudly context = Loudly.getContext();
        if (context.getAction() != null) {
            context.getAction().execute(this);
            context.setAction(null);
        }
        if (context.getTask() != null) {
            context.getTask().attachContext(this);
        }
    }
}
