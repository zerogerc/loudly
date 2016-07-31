package ly.loud.loudly.ui.brand_new.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hannesdorfmann.mosby.mvp.viewstate.RestorableViewState;

public class FeedViewState implements RestorableViewState<FeedView> {

    @Override
    public void saveInstanceState(@NonNull Bundle out) {

    }

    @Override
    public RestorableViewState<FeedView> restoreInstanceState(@Nullable Bundle in) {
        return null;
    }

    @Override
    public void apply(@NonNull FeedView view, boolean retained) {

    }
}
