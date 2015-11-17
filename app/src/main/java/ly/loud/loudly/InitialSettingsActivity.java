package ly.loud.loudly;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import Facebook.FacebookAuthorizer;
import MailRu.MailRuAuthoriser;
import VK.VKAuthorizer;
import base.Authorizer;
import base.Networks;
import base.Tasks;
import util.ResultListener;

public class InitialSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);
    }

    private void setListener(final int network) {
        Loudly.getContext().setListener(new ResultListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                Toast toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                toast.show();
                CheckBox cb = (CheckBox) findViewById(R.id.vk_box);
                switch (network) {
                    case Networks.VK:
                        cb = (CheckBox) findViewById(R.id.vk_box);
                        ;
                        break;
                    case Networks.FB:
                        cb = (CheckBox) findViewById(R.id.vk_box);
                        break;
                    case Networks.MAILRU:
                        cb = (CheckBox) findViewById(R.id.vk_box);
                        break;
                }
                cb.setChecked(true);
            }

            @Override
            public void onFail(Context context, String error) {
                Toast toast = Toast.makeText(context, "Fail", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void VKButtonClick(View v) {
        setListener(Networks.VK);
        Authorizer authorizer = new VKAuthorizer();
        authorizer.createAsyncTask(this).execute();
    }

    public void FBButtonClick(View v) {
        setListener(Networks.FB);
        Authorizer authorizer = new FacebookAuthorizer();
        authorizer.createAsyncTask(this).execute();
    }

    public void MailRuButtonClick(View v) {
        setListener(Networks.MAILRU);
        Authorizer authorizer = new MailRuAuthoriser();
        authorizer.createAsyncTask(this).execute();
    }

    /**
     * Save keys for further use
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tasks.saveKeysTask task = new Tasks.saveKeysTask(this) {
            @Override
            public void ExecuteInUI(Context context, Integer integer) {}
        };
        task.execute();
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
