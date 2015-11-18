package ly.loud.loudly;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


public class PostCreateActivity extends AppCompatActivity {
    private EditText editText;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.post_toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);

        editText = (EditText)findViewById(R.id.post_edit_text);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getCurrentTextColor() == ContextCompat.getColor(context, R.color.light_grey_color)) {
                    if (!s.toString().equals("What's on your mind?")) {
                        editText.setTextColor(ContextCompat.getColor(context, R.color.black_color));
                        CharSequence text = s.subSequence(start, start + count);
                        editText.setText("");
                        editText.append(text);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
