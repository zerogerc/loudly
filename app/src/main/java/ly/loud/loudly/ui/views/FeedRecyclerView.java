package ly.loud.loudly.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import ly.loud.loudly.R;
import ly.loud.loudly.ui.adapters.SpacesItemDecoration;

/**
 * Base class representing feed on the screen.
 */
public class FeedRecyclerView extends RecyclerView {

    private static final String SUPER_STATE = "super_state";
    private static final String VISIBILITY = "visibility";

    @NonNull
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    public FeedRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public FeedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, @StyleRes int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setHasFixedSize(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL) ;
            setLayoutManager(staggeredGridLayoutManager);
        } else {
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            setLayoutManager(staggeredGridLayoutManager);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_landscape_margin);
            addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        }
    }

    @Override
    @NonNull
    public Parcelable onSaveInstanceState () {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        state.putInt(VISIBILITY, getVisibility());
        return state;
    }

    @Override
    public void onRestoreInstanceState (@NonNull Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle)state;
            //noinspection WrongConstant
            setVisibility(savedState.getInt(VISIBILITY, getVisibility()));
            Parcelable superState = savedState.getParcelable(SUPER_STATE);
            super.onRestoreInstanceState(superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
