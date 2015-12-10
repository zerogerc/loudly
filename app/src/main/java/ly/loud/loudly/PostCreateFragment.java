package ly.loud.loudly;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import base.Networks;
import base.Tasks;
import base.Wrap;
import base.attachments.Image;
import base.says.LoudlyPost;
import util.UIAction;
import util.Utils;


public class PostCreateFragment extends Fragment {
    private static String EDIT_TEXT = "EDIT_TEXT";
    private final static int PICK_PHOTO_FROM_GALLERY = 13;

    private NetworksChooseFragment networksChooseFragment;
    private View networksChooseFragmentView;

    private EditText editText;
    private ImageView postImageView;
    private ImageView deleteImageButton;
    private View rootView;
    private static Image postImage;

    public void setListeners() {
        getActivity().findViewById(R.id.new_post_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.show(networksChooseFragment);
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
                MainActivity activity = (MainActivity) getActivity();

                networksChooseFragment.hide();
                activity.onPostCreated();
            }
        };

        networksChooseFragment.setGrayItemClick(grayAction);
        networksChooseFragment.setColorItemsClick(colorAction);
        networksChooseFragment.setPostButtonClick(buttonAction);
    }

    private void initFragment() {
        FragmentManager manager = getActivity().getFragmentManager();
        networksChooseFragment = ((NetworksChooseFragment) manager.findFragmentById(R.id.networks_choose_fragment));

        networksChooseFragmentView = getActivity().findViewById(R.id.networks_choose_fragment);
        networksChooseFragmentView.getBackground().setAlpha(100);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getString(EDIT_TEXT));
        }

        initFragment();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Utils.hidePhoneKeyboard(getActivity());

            postImageView.setImageBitmap(null);
            editText.setText(null);
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
                postImage = new Image(data.getData());
                prepareImageView();
                Glide.with(Loudly.getContext()).load(data.getData())
                        .fitCenter()
                        .into(postImageView);

            }
        }
    }
}
