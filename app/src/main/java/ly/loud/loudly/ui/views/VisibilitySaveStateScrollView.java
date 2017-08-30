package ly.loud.loudly.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class VisibilitySaveStateScrollView extends ScrollView {

    private static final String SUPER_STATE = "super_state";
    private static final String VISIBILITY = "visibility";

    public VisibilitySaveStateScrollView(Context context) {
        super(context);
    }

    public VisibilitySaveStateScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VisibilitySaveStateScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public VisibilitySaveStateScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
