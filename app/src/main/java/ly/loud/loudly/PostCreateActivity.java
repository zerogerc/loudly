package ly.loud.loudly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import base.Post;
import base.Tasks;


public class PostCreateActivity extends AppCompatActivity {
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.post_toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText)findViewById(R.id.post_edit_text);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void clickToast(View v) {
        Toast toast = Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void sendClicked(View v) {

        Tasks.PostUploader uploader = new Tasks.PostUploader(Loudly.getContext().getWraps());

        String text = editText.getText().toString();
        uploader.execute(new Post(text));
        finish();
    }
}
