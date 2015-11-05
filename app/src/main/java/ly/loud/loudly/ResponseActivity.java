package ly.loud.loudly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by ZeRoGerc on 05.11.15.
 */
public class ResponseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent parent = getIntent();
        String request = parent.getStringExtra("REQUEST");
        Intent authActivity = new Intent(this, AuthActivity.class);
        authActivity.putExtra("AUTH_URL", request);
        startActivityForResult(authActivity, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent res = getIntent();
        res.putExtra("RESPONSE", data.getStringExtra("RESPONSE_URL"));

    }
}
