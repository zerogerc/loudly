package ly.loud.loudly.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

public class ScrimCoordinatorLayout extends CoordinatorLayout {

    private static final String SUPER_STATE = "super_state";
    private static final String OPACITY = "opacity";

    @SuppressWarnings("NullableProblems") // Initialized in constructor.
    @NonNull
    private ScrimLayoutDelegate scrimLayoutDelegate;


    public ScrimCoordinatorLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public ScrimCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrimCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scrimLayoutDelegate = new ScrimLayoutDelegate(this);
        scrimLayoutDelegate.init();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        scrimLayoutDelegate.draw(canvas);
    }

    public void setScrimColor(@ColorInt int scrimColor) {
        scrimLayoutDelegate.setScrimColor(scrimColor);
    }

    public void setOpacity(@FloatRange(from = 0.0f, to = 1.0f) float scrimOpacity) {
        scrimLayoutDelegate.setOpacity(scrimOpacity);
    }

    @Override
    @NonNull
    public Parcelable onSaveInstanceState () {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        state.putFloat(OPACITY, scrimLayoutDelegate.getOpacity());
        return state;
    }

    @Override
    public void onRestoreInstanceState (@NonNull Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle)state;
            //noinspection WrongConstant
            setOpacity(((Bundle) state).getFloat(OPACITY, 0));
            Parcelable superState = savedState.getParcelable(SUPER_STATE);
            super.onRestoreInstanceState(superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
