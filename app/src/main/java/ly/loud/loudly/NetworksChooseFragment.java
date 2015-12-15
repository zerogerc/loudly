package ly.loud.loudly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import base.Networks;
import util.UIAction;

/**
 * Created by ZeRoGerc on 11.12.15.
 */
public class NetworksChooseFragment extends Fragment {
    private View rootView;
    private IconsHolder iconsHolder;
    private ImageView postButton;
    private UIAction buttonClick;
    private boolean[] shouldPost = new boolean[Networks.NETWORK_COUNT];

    private UIAction hideAction = null;
    private UIAction showAction = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.network_choose_fragment, container, false);
        iconsHolder = (IconsHolder)rootView.findViewById(R.id.network_choose_icons_holder);
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (showAction != null) {
                showAction.execute(getActivity());
            }
            Log.d("NETWORK", "SHOW");
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                if (Loudly.getContext().getKeyKeeper(i) != null) {
                    shouldPost[i] = true;
                } else {
                    shouldPost[i] = false;
                }
            }
        } else {
            if (hideAction != null) {
                hideAction.execute(getActivity());
            }
            Log.d("NETWORK", "HIDE");
        }
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
}
