package ly.loud.loudly;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by ZeRoGerc on 15.12.15.
 */
public class SplashFragment extends Fragment {
    private View rootView;
    private ImageView image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.splash_fragment, container, false);
        image = ((ImageView) rootView.findViewById(R.id.splash_logo));
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Glide.with(Loudly.getContext()).load(R.drawable.loudly_large)
                .fitCenter()
                .into(image);

        this.show();
        //Listener HERE
    }

    public void show() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.show(this);
        ft.commit();
    }

    public void hide() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(this);
        ft.commit();
        getFragmentManager().popBackStack();
    }
}
