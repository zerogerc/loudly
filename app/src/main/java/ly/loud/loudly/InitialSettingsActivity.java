package ly.loud.loudly;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import Facebook.FacebookAuthorizer;
import MailRu.MailRuAuthoriser;
import VK.VKAuthorizer;
import base.Authorizer;
import base.Tasks;
import util.ResultListener;

public class InitialSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);
    }

    private void setListener() {
        Loudly.getContext().setListener(new ResultListener() {
            @Override
            public void onSuccess(Context context, Object result) {
                Toast toast = Toast.makeText(context, "Success", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onFail(Context context, String error) {
                Toast toast = Toast.makeText(context, "Fail", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void VKButtonClick(View v) {
        setListener();
        Authorizer authorizer = new VKAuthorizer();
        authorizer.createAsyncTask(this).execute();
    }

    public void FBButtonClick(View v) {
        setListener();
        Authorizer authorizer = new FacebookAuthorizer();
        authorizer.createAsyncTask(this).execute();
    }

    public void MailRuButtonClick(View v) {
        setListener();
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
}
