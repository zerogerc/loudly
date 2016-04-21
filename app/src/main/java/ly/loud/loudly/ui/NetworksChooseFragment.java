package ly.loud.loudly.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.ui.views.IconsHolder;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;

/**
 * Created by ZeRoGerc on 11.12.15.
 */
public class NetworksChooseFragment extends DialogFragment {
    public static String TAG = "Networks Choose Fragment";
    private static final String POST_TEXT_KEY = "post_text";
    private static final String POST_IMAGES_KEY = "post_images";

    private static IconsHolder iconsHolder;
    private static boolean[] shouldPost;
    private static int mode = IconsHolder.SHOW_ALL;

    private ImageView postButton;
    private String postText;
    private ArrayList<Image> postImages;


    public static NetworksChooseFragment newInstance(String text, ArrayList<Image> images) {
        Bundle args = new Bundle();

        args.putString(POST_TEXT_KEY, text);
        args.putParcelableArrayList(POST_IMAGES_KEY, images);

        NetworksChooseFragment fragment = new NetworksChooseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void setListeners(final View rootView) {
        iconsHolder.setGrayItemClick(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                if (Loudly.getContext().getKeyKeeper(network) == null)
                    return;
                setShouldPostTo(network, true);
            }
        });

        iconsHolder.setColorItemsClick(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                int network = ((int) params[0]);
                if (Loudly.getContext().getKeyKeeper(network) == null)
                    return;
                setShouldPostTo(network, false);
            }
        });

        postButton = (ImageView)rootView.findViewById(R.id.network_choose_button);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoudlyPost post = new LoudlyPost(postText);
                if (postImages.size() > 0) {
                    post.addAttachment(postImages.get(0));
                }

                ArrayList<Wrap> wraps = new ArrayList<>();
                for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                    if (Loudly.getContext().getKeyKeeper(i) != null && shouldPost[i]) {
                        wraps.add(Networks.makeWrap(i));
                    }
                }

                Tasks.PostUploader uploader = new Tasks.PostUploader(post,
                        wraps.toArray(new Wrap[0]));
                uploader.execute(post);

                PostCreateFragment.self.dismiss();
                dismiss();
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.network_choose_fragment, null);

        iconsHolder = (IconsHolder)rootView.findViewById(R.id.network_choose_icons_holder);

        //TODO: remove
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Utils.hidePhoneKeyboard(getActivity());

        iconsHolder.prepareView(IconsHolder.SHOW_ONLY_AVAILABLE);

        if (savedInstanceState == null) {
            shouldPost = new boolean[Networks.NETWORK_COUNT];
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                shouldPost[i] = true;
            }
        }

        for (int i = 0; i < shouldPost.length; i++) {
            setShouldPostTo(i, shouldPost[i]);
        }

        postText = getArguments().getString(POST_TEXT_KEY);
        postImages = getArguments().getParcelableArrayList(POST_IMAGES_KEY);

        setListeners(rootView);

        builder.setView(rootView);

        return builder.create();
    }

    public void setShouldPostTo(int network, boolean state) {
        if (state) {
            iconsHolder.setVisible(network);
        } else {
            iconsHolder.setInvisible(network);
        }
        shouldPost[network] = state;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(POST_TEXT_KEY, postText);
        outState.putParcelableArrayList(POST_IMAGES_KEY, postImages);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
