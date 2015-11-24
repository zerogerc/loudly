package ly.loud.loudly;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import base.Post;
import base.Tasks;
import base.attachments.Image;
import util.UtilsBundle;


public class PostCreateActivity extends AppCompatActivity {
    private final static int PICK_PHOTO_FROM_GALLERY = 13;

    private EditText editText;
    private ImageView postImageView;
    private static Image postImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.post_toolbar); // Attaching the layout to the main_toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText)findViewById(R.id.post_edit_text);
        postImageView = (ImageView)findViewById(R.id.new_post_post_image);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void clickToast(View v) {
        Toast toast = Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void sendClicked(View v) {
        Tasks.PostUploader uploader = new Tasks.PostUploader(Loudly.getContext().getWraps());

        String text = editText.getText().toString();
        Post post = new Post(text);
        if (postImage != null) {
            post.addAttachment(postImage);
        }
        uploader.execute(post);
        finish();
    }

    public void pickImage(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.e("IMG_LOAD_TAG", "Data to received");
            } else {
                postImage = new Image(data.getData());
                int desiredWidth = ((RelativeLayout)postImageView.getParent()).getWidth();
                int desiredHeight = ((RelativeLayout)postImageView.getParent()).getHeight();
                Bitmap bitmap = UtilsBundle.loadBitmap(data.getData(), desiredWidth, desiredHeight);
                postImageView.setImageBitmap(bitmap);
            }
        }
    }
}
