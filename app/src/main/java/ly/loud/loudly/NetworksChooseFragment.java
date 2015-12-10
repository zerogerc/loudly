package ly.loud.loudly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
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
                }
            }
        });

        return rootView;
    }

    public void setPostButtonClick(UIAction action) {
        this.buttonClick = action;
    }

    public void setShouldPostTo(int network, boolean state) {
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
            for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                if (Loudly.getContext().getKeyKeeper(i) != null) {
                    shouldPost[i] = true;
                } else {
                    shouldPost[i] = false;
                }
            }
        }
    }

    public void setColorItemsClick(UIAction action) {
        iconsHolder.setColorItemsClick(action);
    }

    public void setGrayItemClick(UIAction action) {
        iconsHolder.setGrayItemClick(action);
    }
}
