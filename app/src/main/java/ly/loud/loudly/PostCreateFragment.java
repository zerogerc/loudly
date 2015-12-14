package ly.loud.loudly;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import base.Networks;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import base.attachments.LoudlyImage;
import base.says.LoudlyPost;
import util.UIAction;
import util.Utils;


public class PostCreateFragment extends Fragment {
    private static String EDIT_TEXT = "EDIT_TEXT";
    private final static int PICK_PHOTO_FROM_GALLERY = 13;
    private final static int REQUEST_PHOTO_FROM_CAMERA = 52;

    private NetworksChooseFragment networksChooseFragment;

    private EditText editText;
    private ImageView postImageView;
    private ImageView deleteImageButton;
    private View rootView;
    private FrameLayout background;
    private static Image postImage;

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

    public void setListeners() {
        getActivity().findViewById(R.id.new_post_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                Utils.hidePhoneKeyboard(getActivity());
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                        R.anim.slide_in_left, R.anim.slide_out_right);
                ft.show(networksChooseFragment);
                ft.addToBackStack(null);
                ft.commit();
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

//        rootView.findViewById(R.id.new_post_gallery_button).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        ImageView view = (ImageView) v;
//                        //overlay is black with transparency of 0x77 (119)
//                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
//                        view.invalidate();
//                        break;
//                    }
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_CANCEL: {
//                        ImageView view = (ImageView) v;
//                        //clear the overlay
//                        view.getDrawable().clearColorFilter();
//                        view.invalidate();
//                        break;
//                    }
//                }
//                return false;
//            }
//        });


        getActivity().findViewById(R.id.new_post_camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hidePhoneKeyboard(getActivity());
                dispatchTakePictureIntent();
            }
        });
    }

    private void setNetworksChooseListeners() {
        UIAction grayAction = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                if (Loudly.getContext().getKeyKeeper(network) == null)
                    return;
                networksChooseFragment.setShouldPostTo(network, true);
            }
        };
        UIAction colorAction = new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                if (Loudly.getContext().getKeyKeeper(network) == null)
                    return;
                networksChooseFragment.setShouldPostTo(network, false);
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
                    if (networksChooseFragment.shouldPostTo(i)) {
                        wraps.add(Wrap.makeWrap(i));
                    }
                }
                Tasks.PostUploader uploader = new Tasks.PostUploader(post, MainActivity.posts,
                        wraps.toArray(new Wrap[0]));
                uploader.execute(post);

                getActivity().getFragmentManager().popBackStack();
            }
        };

        networksChooseFragment.setGrayItemClick(grayAction);
        networksChooseFragment.setColorItemsClick(colorAction);
        networksChooseFragment.setPostButtonClick(buttonAction);
    }

    private void initFragment() {
        FragmentManager manager = getActivity().getFragmentManager();
        networksChooseFragment = ((NetworksChooseFragment) manager.findFragmentById(R.id.networks_choose_fragment));

        networksChooseFragment.setHideAction(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                editText.setFocusableInTouchMode(true);
                editText.setFocusable(true);
                setOverShadow(false);
            }
        });

        networksChooseFragment.setShowAction(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                editText.setFocusableInTouchMode(false);
                editText.setFocusable(false);
                setOverShadow(true);
            }
        });

        FragmentTransaction ft = manager.beginTransaction();
        ft.hide(networksChooseFragment);
        ft.commit();

        setNetworksChooseListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.activity_new_post, container, false);
        editText = (EditText)rootView.findViewById(R.id.new_post_edit_text);
        postImageView = (ImageView)rootView.findViewById(R.id.picture_with_cross_image);
        deleteImageButton = (ImageView)rootView.findViewById(R.id.picture_with_cross_clear);
        deleteImageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteImage();
                    }
                }
        );

        postImage = null;
        background = ((FrameLayout) rootView.findViewById(R.id.new_post_background));
        setOverShadow(false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getString(EDIT_TEXT));
        }

        setListeners();
        initFragment();
        getFragmentManager().popBackStack();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Utils.hidePhoneKeyboard(getActivity());

            postImageView.setImageBitmap(null);
            editText.setText(null);
            setOverShadow(true);
        } else {
            clearImageView();

            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) editText.getLayoutParams());
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            editText.requestFocus();

            setOverShadow(false);
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
        getActivity();
        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            postImage = new LoudlyImage(currentImageUri);
            galleryAddPic();
            prepareImageView();
            Glide.with(Loudly.getContext()).load(currentImageUri)
                    .fitCenter()
                    .into(postImageView);

        }
    }

    public void setOverShadow(boolean flag) {
        if (flag) {
            background.setAlpha(1);
            background.getBackground().setAlpha(100);
            background.setClickable(true);
        } else {
            background.setAlpha(0);
            background.setClickable(false);
        }
    }

    public void show() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.show(this);
        ft.commit();
    }
}
