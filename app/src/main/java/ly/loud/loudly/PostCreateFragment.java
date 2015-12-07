package ly.loud.loudly;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import base.says.LoudlyPost;
import base.Tasks;
import base.attachments.Image;
import util.Utils;


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

                String text = editText.getText().toString();
                LoudlyPost post = new LoudlyPost(text);
                if (postImage != null) {
                    post.addAttachment(postImage);
                    postImage = null;
                }

                Tasks.PostUploader uploader = new Tasks.PostUploader(post, MainActivity.posts,
                        Loudly.getContext().getWraps());
                uploader.execute(post);
                postImageView.setImageBitmap(null);
                editText.setText(null);
                MainActivity activity = (MainActivity)getActivity();
                activity.onPostCreated();
            }
        });

        getActivity().findViewById(R.id.new_post_gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Utils.hidePhoneKeyboard(getActivity());
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
            Utils.hidePhoneKeyboard(getActivity());
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
                final float scale = getResources().getDisplayMetrics().density;
                int dpWidthInPx = (int) (72 * scale);
                int dpHeightInPx = (int) (72 * scale);
                int margins = (int) (4 * scale);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) postImageView.getLayoutParams();
                layoutParams.width = dpWidthInPx;
                layoutParams.height = dpHeightInPx;
                layoutParams.setMargins(margins, margins, margins, margins);
                postImageView.setLayoutParams(layoutParams);
                Picasso.with(Loudly.getContext()).load(data.getData())
                        .resize(Utils.dpToPx(72), Utils.dpToPx(72))
                        .into(postImageView);
            }
        }
    }
}
