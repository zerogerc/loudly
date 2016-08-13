package ly.loud.loudly.ui.views;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import ly.loud.loudly.R;

public class PostButton extends Button {

    private final static String SUPER_STATE = "super_state";
    private final static String COLOR_STATE = "colorState";
    private final static String CLICKABLE = "clickable";

    private final static int STATE_LIGHT = 0;
    private final static int STATE_BRIGHT = 1;

    @ColorInt
    private int backgroundColorLight;

    @ColorInt
    private int backgroundColorBright;

    private int colorState = STATE_LIGHT;

    public PostButton(Context context) {
        super(context);
        init();
    }

    public PostButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PostButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public PostButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    @NonNull
    public Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        state.putInt(COLOR_STATE, colorState);
        state.putBoolean(CLICKABLE, isClickable());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle) state;
            //noinspection WrongConstant
            setClickable(((Bundle) state).getBoolean(CLICKABLE, true));
            colorState = savedState.getInt(COLOR_STATE);
            applyState();
            Parcelable superState = savedState.getParcelable(SUPER_STATE);
            super.onRestoreInstanceState(superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void init() {
        backgroundColorLight = ContextCompat.getColor(getContext(), R.color.divider);
        backgroundColorBright = ContextCompat.getColor(getContext(), R.color.accent);
    }

    private void applyState() {
        if (colorState == STATE_LIGHT) {
            setBackgroundColor(backgroundColorLight);
        } else {
            setBackgroundColor(backgroundColorBright);
        }
    }

    private void setStateWithAnimations(
            int state,
            boolean clickable,
            @ColorInt int colorFrom,
            @ColorInt int colorTo
    ) {
        if (colorState == state) {
            return;
        }
        setClickable(clickable);
        colorState = state;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            ObjectAnimator animator = ObjectAnimator.ofArgb(this, "backgroundColor", colorFrom, colorTo)
                    .setDuration(500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        } else {
            setBackgroundColor(colorTo);
        }
    }

    /**
     * Change state of button to bright with animations.
     */
    public void setStateBright() {
        setStateWithAnimations(STATE_BRIGHT, true, backgroundColorLight, backgroundColorBright);
    }

    /**
     * Change state of button to light with animations.
     */
    public void setStateLight() {
        setStateWithAnimations(STATE_LIGHT, false, backgroundColorBright, backgroundColorLight);
    }
}
