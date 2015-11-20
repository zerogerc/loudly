package ly.loud.loudly;

import android.content.Context;
import android.content.Intent;
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
import util.AttachableReceiver;
import util.ResultListener;

public class InitialSettingsActivity extends AppCompatActivity {
    private static AttachableReceiver authReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);
        int[] checkboxes = {R.id.fb_box, -1, -1, R.id.vk_box, -1, R.id.mail_ru_box};
        for (int i = 0; i < checkboxes.length; i++) {
            int id = checkboxes[i];
            if (id == -1) {
                continue;
            }
            findViewById(id).setEnabled(false);
            ((CheckBox) findViewById(id)).setChecked(Loudly.getContext().getKeyKeeper(i) != null);
        }
    }

    // That's here because of 3 different click listeners
    private void startReceiver() {
        authReceiver = new AttachableReceiver(this, Loudly.AUTHORIZATION_FINISHED) {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra("success", false);
                if (success) {
                    Toast toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                    toast.show();
                    int network = intent.getIntExtra("network", -1);
                    CheckBox cb = (CheckBox) findViewById(R.id.vk_box);
                    switch (network) {
                        case Networks.VK:
                            cb = (CheckBox) findViewById(R.id.vk_box);
                            break;
                        case Networks.FB:
                            cb = (CheckBox) findViewById(R.id.fb_box);
                            break;
                        case Networks.MAILRU:
                            cb = (CheckBox) findViewById(R.id.mail_ru_box);
                            break;
                    }
                    cb.setChecked(true);
                } else {
                    String error = intent.getStringExtra("error");
                    Toast toast = Toast.makeText(context, "Fail: " + error, Toast.LENGTH_SHORT);
                    toast.show();
                }
                stop();
                authReceiver = null;
            }
        };
    }

    // ToDo: make buttons onclickable during authorization

    public void VKButtonClick(View v) {
        startReceiver();
        Authorizer authorizer = new VKAuthorizer();
        authorizer.createAsyncTask(this).execute();
    }

    public void FBButtonClick(View v) {
        startReceiver();
        Authorizer authorizer = new FacebookAuthorizer();
        authorizer.createAsyncTask(this).execute();
    }

    public void MailRuButtonClick(View v) {
        startReceiver();
        Authorizer authorizer = new MailRuAuthoriser();
        authorizer.createAsyncTask(this).execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authReceiver != null) {
            authReceiver.detach();
        }
    }

    /**
     * Save keys for further use
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tasks.SaveKeysTask task = new Tasks.SaveKeysTask(this);
        task.execute();
    }
}
