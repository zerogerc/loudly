package ly.loud.loudly.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

public class ScrimCoordinatorLayout extends CoordinatorLayout {

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
}
