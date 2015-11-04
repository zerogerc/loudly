package ly.loud.loudly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void authCall(View v) {
        Intent temp = new Intent(this, AuthActivity.class);
        startActivityForResult(temp, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Log.d("MAIN_TAG", data.getStringExtra("RESULT"));
                } else {
                    Log.d("MAIN_TAG", data.getStringExtra("RESULT"));
                }
                break;
        }
    }
}
