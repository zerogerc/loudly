package ly.loud.loudly.ui.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;

public class ScrimLayoutDelegate {

    private final View layout;

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    @ColorInt
    private int scrimColor = DEFAULT_SCRIM_COLOR;

    @FloatRange(from = 0.0f, to = 1.0f)
    private float scrimOpacity;

    @NonNull
    private final Paint scrimPaint = new Paint();

    public ScrimLayoutDelegate(@NonNull View layout) {
        this.layout = layout;
    }

    public void init() {
        layout.setWillNotDraw(false);
    }

    public void setScrimColor(@ColorInt int scrimColor) {
        this.scrimColor = scrimColor;
    }

    public void setOpacity(@FloatRange(from = 0.0f, to = 1.0f) float scrimOpacity) {
        this.scrimOpacity = scrimOpacity;
        layout.invalidate();
    }

    public float getOpacity() {
        return scrimOpacity;
    }

    public void draw(@NonNull Canvas canvas) {
        final int baseAlpha = (scrimColor & 0xff000000) >>> 24;
        final int imag = (int) (baseAlpha * scrimOpacity);
        final int color = imag << 24 | (scrimColor & 0xffffff);
        scrimPaint.setColor(color);

        canvas.drawRect(layout.getLeft(), layout.getTop(), layout.getRight(), layout.getBottom(), scrimPaint);
    }
}
