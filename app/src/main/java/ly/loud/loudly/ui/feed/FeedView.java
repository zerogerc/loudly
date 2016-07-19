package ly.loud.loudly.ui.feed;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import ly.loud.loudly.R;
import ly.loud.loudly.ui.adapter.SpacesItemDecoration;

/**
 * Base class representing feed on the screen.
 */
public class FeedView extends RecyclerView {
    public FeedView(@NonNull Context context) {
        this(context, null);
    }

    public FeedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedView(@NonNull Context context, @Nullable AttributeSet attrs, @StyleRes int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        setHasFixedSize(true);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_landscape_margin);
            addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        }
        setItemAnimator(itemAnimator);
    }
}
