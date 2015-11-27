package ly.loud.loudly;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import base.Post;
import base.Tasks;
import base.attachments.Image;
import util.UtilsBundle;


public class PostCreateFragment extends Fragment {
    private static String EDIT_TEXT = "EDIT_TEXT";
    private final static int PICK_PHOTO_FROM_GALLERY = 13;

    private EditText editText;
    private ImageView postImageView;
    private View rootView;
    private static Image postImage;

    public void setListeners() {
        getActivity().findViewById(R.id.new_post_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tasks.PostUploader uploader = new Tasks.PostUploader(Loudly.getContext().getWraps());

                String text = editText.getText().toString();
                Post post = new Post(text);
                if (postImage != null) {
                    post.addAttachment(postImage);
                    postImage = null;
                }
                uploader.execute(post);
                postImageView.setImageBitmap(null);
                editText.setText(null);
                MainActivity activity = (MainActivity)getActivity();
                UtilsBundle.hidePhoneKeypad(rootView);
                activity.onPostCreated();
            }
        });

        getActivity().findViewById(R.id.new_post_gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO_FROM_GALLERY);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.activity_new_post, container, false);
        editText = (EditText)rootView.findViewById(R.id.new_post_edit_text);
        postImageView = (ImageView)rootView.findViewById(R.id.new_post_post_image);
        postImage = null;

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getString(EDIT_TEXT));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            UtilsBundle.hidePhoneKeypad(this.rootView);
        } else {
            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            editText.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EDIT_TEXT, editText.getText().toString());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.e("IMG_LOAD_TAG", "Data to received");
            } else {
                postImage = new Image(data.getData());
                Bitmap bitmap = UtilsBundle.loadBitmap(data.getData(), UtilsBundle.getDefaultScreenWidth(), UtilsBundle.getDefaultScreenWidth());
                postImage.setBitmap(bitmap);
                final float scale = getResources().getDisplayMetrics().density;
                int dpWidthInPx  = (int) (72 * scale);
                int dpHeightInPx = (int) (72 * scale);
                int margins = (int) (4 * scale);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) postImageView.getLayoutParams();
                layoutParams.width = dpWidthInPx;
                layoutParams.height = dpHeightInPx;
                layoutParams.setMargins(margins, margins, margins, margins);
                postImageView.setLayoutParams(layoutParams);
                postImageView.setImageBitmap(bitmap);
            }
        }
    }
}
