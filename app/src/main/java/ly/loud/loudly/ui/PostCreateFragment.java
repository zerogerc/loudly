package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.attachments.LoudlyImage;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;


public class PostCreateFragment extends DialogFragment {
    private static String EDIT_TEXT = "EDIT_TEXT";
    private static String TAG = "Post Create Fragment";
    private final static int PICK_PHOTO_FROM_GALLERY = 13;
    private final static int REQUEST_PHOTO_FROM_CAMERA = 52;

    private EditText editText;
    private ImageView postImageView;
    private static Image postImage;

    private View rootView;

    private Uri currentImageUri;

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, currentImageUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        String mCurrentPhotoPath;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("IMAGE", storageDir + imageFileName + ".jpg");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("POST_CREATE", "COULDN'T CREATE FILE PATH", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentImageUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        currentImageUri);
                startActivityForResult(takePictureIntent, REQUEST_PHOTO_FROM_CAMERA);
            }
        }
    }

    public boolean existsAvailableNetworks() {
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (Loudly.getContext().getKeyKeeper(i) != null) {
                return true;
            }
        }
        return false;
    }

    public void setListeners() {
        rootView.findViewById(R.id.new_post_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existsAvailableNetworks()) {
                    showNetworksChooseFragment();
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.main_layout),
                            "You must be logged in at least one network", Snackbar.LENGTH_SHORT)
                            .show();
                }

            }
        });

        rootView.findViewById(R.id.new_post_gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Utils.hidePhoneKeyboard(getActivity());
                startActivityForResult(intent, PICK_PHOTO_FROM_GALLERY);
            }
        });

        rootView.findViewById(R.id.new_post_camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hidePhoneKeyboard(getActivity());
                dispatchTakePictureIntent();
            }
        });
    }

    private void showNetworksChooseFragment() {
        final NetworksChooseFragment fragment = new NetworksChooseFragment();

        UIAction grayAction = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                if (Loudly.getContext().getKeyKeeper(network) == null)
                    return;
                fragment.setShouldPostTo(network, true);
            }
        };
        UIAction colorAction = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                if (Loudly.getContext().getKeyKeeper(network) == null)
                    return;
                fragment.setShouldPostTo(network, false);
            }
        };
        UIAction buttonAction = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                String text = editText.getText().toString();
                LoudlyPost post = new LoudlyPost(text);
                if (postImage != null) {
                    post.addAttachment(postImage);
                    postImage = null;
                }

                ArrayList<Wrap> wraps = new ArrayList<>();
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    // ToDO: upload to only selected networks
                    if (Loudly.getContext().getKeyKeeper(i) != null && fragment.shouldPost(i)) {
                        wraps.add(Networks.makeWrap(i));
                    }
                }

                Tasks.PostUploader uploader = new Tasks.PostUploader(post, MainActivity.posts,
                        wraps.toArray(new Wrap[0]));
                uploader.execute(post);

                dismiss();
            }
        };

        fragment.setGrayItemClick(grayAction);
        fragment.setColorItemsClick(colorAction);
        fragment.setPostButtonClick(buttonAction);

        fragment.show(getFragmentManager(), NetworksChooseFragment.TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        rootView = inflater.inflate(R.layout.new_post_fragment, null);

        editText = (EditText)rootView.findViewById(R.id.new_post_edit_text);
        postImageView = (ImageView)rootView.findViewById(R.id.picture_with_cross_image);
        ImageView deleteImageButton = (ImageView)rootView.findViewById(R.id.picture_with_cross_clear);
        deleteImageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteImage();
                    }
                }
        );

        postImage = null;

        setListeners();

        builder.setView(rootView);

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
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

            postImageView.setImageBitmap(null);
            editText.setText(null);
        } else {
            clearImageView();

            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) editText.getLayoutParams());
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            editText.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EDIT_TEXT, editText.getText().toString());
    }

    private void deleteImage() {
        if (postImage == null) {
            return;
        }

        LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) editText.getLayoutParams());
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;

        RelativeLayout layout = (RelativeLayout)rootView.findViewById(R.id.new_post_list_item);
        layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.setMargins(0, 0, 0, 0);
        layout.setLayoutParams(layoutParams);
        postImageView.setImageBitmap(null);
        postImage = null;
    }

    private void clearImageView() {
        RelativeLayout layout = (RelativeLayout)rootView.findViewById(R.id.new_post_list_item);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.setMargins(0, 0, 0, 0);
        layout.setLayoutParams(layoutParams);
    }

    private void prepareImageView() {
        final float scale = getResources().getDisplayMetrics().density;
        int margins = (int) (16 * scale);

        LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) editText.getLayoutParams());
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        RelativeLayout layout = (RelativeLayout)rootView.findViewById(R.id.new_post_list_item);
        layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.width = postImageView.getLayoutParams().width;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(margins, margins, margins, margins);
        layout.setLayoutParams(layoutParams);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.e("IMG_LOAD_TAG", "Data to received");
            } else {
                postImage = new LoudlyImage(data.getData());
                prepareImageView();
                Glide.with(Loudly.getContext()).load(data.getData())
                        .fitCenter()
                        .into(postImageView);

            }
        }
        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            postImage = new LoudlyImage(currentImageUri);
            galleryAddPic();
            prepareImageView();
            Glide.with(Loudly.getContext()).load(currentImageUri)
                    .fitCenter()
                    .into(postImageView);

        }
    }

    public static PostCreateFragment showPostCreate(Activity activity) {
        PostCreateFragment newFragment = new PostCreateFragment();
        newFragment.show(activity.getFragmentManager(), TAG);
        return newFragment;
    }
}
