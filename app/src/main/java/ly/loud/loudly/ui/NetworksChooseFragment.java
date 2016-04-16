package ly.loud.loudly.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;

/**
 * Created by ZeRoGerc on 11.12.15.
 */
public class NetworksChooseFragment extends DialogFragment {
    public static String TAG = "Networks Choose Fragment";

    private View rootView;
    private IconsHolder iconsHolder;
    private ImageView postButton;

    private UIAction sendClick;
    private UIAction grayClick;
    private UIAction coloredClick;

    private boolean[] shouldPost;

    private int mode = IconsHolder.SHOW_ALL;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        rootView = inflater.inflate(R.layout.network_choose_fragment, null);

        iconsHolder = (IconsHolder)rootView.findViewById(R.id.network_choose_icons_holder);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Utils.hidePhoneKeyboard(getActivity());

        iconsHolder.prepareView(IconsHolder.SHOW_ONLY_AVAILABLE);

        postButton = (ImageView)rootView.findViewById(R.id.network_choose_button);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendClick != null) {
                    sendClick.execute(Loudly.getContext());
                    dismiss();
                }
            }
        });

        iconsHolder.setGrayItemClick(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                if (grayClick != null) {
                    grayClick.execute(context, params);
                }
            }
        });

        iconsHolder.setColorItemsClick(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                if (coloredClick != null) {
                    coloredClick.execute(context, params);
                }
            }
        });

        shouldPost = new boolean[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            shouldPost[i] = true;
        }

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

    public void setColorItemsClick(UIAction action) {
        coloredClick = action;
    }

    public void setGrayItemClick(UIAction action) {
        grayClick = action;
    }

    public void setPostButtonClick(UIAction action) {
        sendClick = action;
    }

    public boolean shouldPost(int network) {
        return shouldPost[network];
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
