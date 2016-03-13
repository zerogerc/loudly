package ly.loud.loudly.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import ly.loud.loudly.base.Networks;
import ly.loud.loudly.R;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;

/**
 * Created by ZeRoGerc on 11.12.15.
 */
public class NetworksChooseFragment extends Fragment {
    private View rootView;
    private IconsHolder iconsHolder;
    private ImageView postButton;
    private UIAction buttonClick;
    private boolean[] shouldPost = new boolean[Networks.NETWORK_COUNT];

    private int mode = IconsHolder.SHOW_ALL;

    private UIAction hideAction = null;
    private UIAction showAction = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.network_choose_fragment, container, false);
        iconsHolder = (IconsHolder)rootView.findViewById(R.id.network_choose_icons_holder);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Utils.hidePhoneKeyboard(getActivity());

        iconsHolder.prepareView(IconsHolder.SHOW_ONLY_AVAILABLE);

        postButton = (ImageView)rootView.findViewById(R.id.network_choose_button);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonClick != null) {
                    buttonClick.execute(Loudly.getContext());
                    getActivity().getFragmentManager().popBackStack();
                }
            }
        });

        rootView.findViewById(R.id.network_choose_card).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!rootView.findViewById(R.id.network_choose_card).onTouchEvent(event)) {
                        getActivity().getFragmentManager().popBackStack();
                    }
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (showAction != null) {
            showAction.execute(getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hideAction != null) {
            hideAction.execute(getActivity());
        }
    }

    public void setPostButtonClick(UIAction action) {
        this.buttonClick = action;
    }

    public void setShouldPostTo(int network, boolean state) {
        if (state) {
            iconsHolder.setVisible(network);
        } else {
            iconsHolder.setInvisible(network);
        }
        shouldPost[network] = state;
    }

    public boolean shouldPostTo(int network) {
        return shouldPost[network];
    }

    public void hide() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(this);
        ft.commit();
    }

    public void setVisible(int network) {
        iconsHolder.setVisible(network);
    }

    public void setInvisible(int network) {
        iconsHolder.setInvisible(network);
    }

    public void setColorItemsClick(UIAction action) {
        iconsHolder.setColorItemsClick(action);
    }

    public void setGrayItemClick(UIAction action) {
        iconsHolder.setGrayItemClick(action);
    }

    public void setHideAction(UIAction action) {
        this.hideAction = action;
    }

    public void setShowAction(UIAction action) {
        this.showAction = action;
    }

    public IconsHolder getIconsHolder() {
        return this.iconsHolder;
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d("NETWORK", "START");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("NETWORK", "RESUME");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("NETWORK", "STOP");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("NETWORK", "PAUSE");
    }

    private static void show(Activity activity, NetworksChooseFragment fragment) {
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_left);
        transaction.replace(R.id.new_post_fragment_container, fragment);
        transaction.commit();

        fragment.setMode(IconsHolder.SHOW_ONLY_AVAILABLE);

        activity.getFragmentManager().executePendingTransactions();
    }

    public static NetworksChooseFragment showNetworksChoose(Activity activity, UIAction showAction, UIAction hideAction) {
        NetworksChooseFragment newFragment = new NetworksChooseFragment();
        newFragment.showAction = showAction;
        newFragment.hideAction = hideAction;
        show(activity, newFragment);
        return newFragment;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
