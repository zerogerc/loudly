package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.attachments.LoudlyImage;
import ly.loud.loudly.ui.brand_new.post.NetworkChooseFragment;
import ly.loud.loudly.util.Utils;


public class PostCreateFragment extends DialogFragment {
    public static final String TAG = "Post Create Fragment";
    private static final String CURRENT_IMAGE_URI = "image_uri";
    private static final String POST_IMAGES_KEY = "post_images";

    private final static int PICK_PHOTO_FROM_GALLERY = 13;
    private final static int REQUEST_PHOTO_FROM_CAMERA = 52;

    //TODO: try to avoid such style
    protected static PostCreateFragment self;

    private View rootView;
    private EditText editText;
    private static ImageView postImageView;
    private ArrayList<Image> postImages;

    private static Uri currentImageUri;

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
        rootView.findViewById(R.id.new_post_send_button).setOnClickListener(v -> {
            if (existsAvailableNetworks()) {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                DialogFragment dialogFragment = new NetworkChooseFragment();
                dialogFragment.show(getActivity().getSupportFragmentManager(), dialogFragment.getTag());
            } else {
                Snackbar.make(getActivity().findViewById(R.id.main_layout),
                        "You must be logged in at least one network", Snackbar.LENGTH_SHORT)
                        .show();
            }

        });

        rootView.findViewById(R.id.material_new_post_fragment_gallery_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Utils.hidePhoneKeyboard(getActivity());
                startActivityForResult(intent, PICK_PHOTO_FROM_GALLERY);
            }
        });

        rootView.findViewById(R.id.material_new_post_fragment_camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hidePhoneKeyboard(getActivity());
                dispatchTakePictureIntent();
            }
        });
    }

    private void showNetworksChooseFragment() {
        NetworksChooseFragment fragment = NetworksChooseFragment.newInstance(editText.getText().toString(), postImages);
        fragment.show(getFragmentManager(), NetworksChooseFragment.TAG);
    }

    public static PostCreateFragment newInstance() {
        Bundle args = new Bundle();
        args.putParcelableArrayList(POST_IMAGES_KEY, new ArrayList<Image>());
        PostCreateFragment fragment = new PostCreateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        rootView = inflater.inflate(R.layout.new_post_fragment, null);

        editText = (EditText) rootView.findViewById(R.id.material_new_post_fragment_edit_text);
        postImageView = (ImageView) rootView.findViewById(R.id.picture_with_cross_image);
        ImageView deleteImageButton = (ImageView) rootView.findViewById(R.id.picture_with_cross_clear);
        deleteImageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteImage();
                    }
                }
        );

        if (getArguments() != null) {
//            currentImageUri = getArguments().getParcelable(CURRENT_IMAGE_URI);
            postImages = getArguments().getParcelableArrayList(POST_IMAGES_KEY);
        } else {
            postImages = new ArrayList<>();
        }

        builder.setView(rootView);


        setListeners();
        showImages();

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        self = this;

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (currentImageUri != null) {
            outState.putParcelable(CURRENT_IMAGE_URI, currentImageUri);
        }
        outState.putParcelableArrayList(POST_IMAGES_KEY, postImages);

        super.onSaveInstanceState(outState);
    }

    private void deleteImage() {
        if (postImages.size() == 0) {
            return;
        }

        LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) editText.getLayoutParams());
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;

        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.new_post_list_item);
        layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        layoutParams.setMargins(0, 0, 0, 0);
        layout.setLayoutParams(layoutParams);
        postImageView.setImageBitmap(null);
        postImages.clear();
    }

    private void clearImageView() {
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.new_post_list_item);
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

        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.new_post_list_item);
        layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.width = postImageView.getLayoutParams().width;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(margins, margins, margins, margins);
        layout.setLayoutParams(layoutParams);
    }

    private void showImages() {
        if (postImages.size() == 0) {
            return;
        }
        galleryAddPic();
        prepareImageView();
        Glide.with(Loudly.getContext()).load(postImages.get(0).getUri())
                .fitCenter()
                .into(postImageView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.e("IMG_LOAD_TAG", "Data to received");
            } else {
                if (data.getData() == null) {
                    return;
                }
                postImages.add(new LoudlyImage(data.getData()));
                prepareImageView();
                Glide.with(Loudly.getContext()).load(data.getData())
                        .fitCenter()
                        .into(postImageView);
            }
        }
        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            postImages.clear();
            postImages.add(new LoudlyImage(currentImageUri));
            showImages();
        }
    }
}
