package ly.loud.loudly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import base.Networks;
import util.UtilsBundle;

/**
 * Created by ZeRoGerc on 29.11.15.
 */
public class IconsHolder extends View {
    private SettingsActivity activity;
    private int iconWidth;
    private int iconHeight;
    private int parentWidth;
    private int columns = 0;
    private int rows = 0;
    private int margin = 0;
    private Rect[] zones = new Rect[Networks.NETWORK_COUNT];
    private boolean[] isVisible = new boolean[Networks.NETWORK_COUNT];

    public IconsHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (SettingsActivity)context;

        for (int network = 0; network < Networks.NETWORK_COUNT; network++) {
            if (Loudly.getContext().getKeyKeeper(network) != null) {
                isVisible[network] = true;
            } else {
                isVisible[network] = false;
            }
        }

        Bitmap bitmap = UtilsBundle.getIconByNetwork(Networks.FB);
        iconHeight = bitmap.getHeight();
        iconWidth = bitmap.getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        columns = Math.min(Networks.NETWORK_COUNT, parentWidth / iconWidth);
        margin = (parentWidth - (iconWidth) * columns) / (columns + 1);
        rows = Networks.NETWORK_COUNT / columns + 1;
        if (Networks.NETWORK_COUNT % columns == 0) {
            rows--;
        }

        setMeasuredDimension(parentWidth, iconHeight * rows + margin * (rows - 1));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cur_w = margin;
        int cur_h = 0;
        for (int network = 0; network < Networks.NETWORK_COUNT; network++) {
            Bitmap bitmap = null;
            Log.d("ICONS", "image");
            if (isVisible[network]) {
                bitmap = UtilsBundle.getIconByNetwork(network);
            } else {
                Bitmap bm = UtilsBundle.getIconByNetwork(network);
                bitmap = UtilsBundle.toGrayscale(bm);
            }
            zones[network] = new Rect(cur_w, cur_h, cur_w + iconWidth, cur_h + iconHeight);
            canvas.drawBitmap(bitmap, cur_w, cur_h, null);

            cur_w += iconWidth + margin;
            if ((network + 1) % columns == 0) {
                cur_h += iconHeight + margin;
                cur_w = margin;
            }
        }
    }

    public void setVisible(int network) {
        isVisible[network] = true;
        invalidate();
    }

    public void setInvisible(int network) {
        isVisible[network] = false;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        if (zones[Networks.FB].contains(x, y)) {
            if (isVisible[Networks.FB]) {
                activity.LogoutClick(Networks.FB);
                setInvisible(Networks.FB);
            } else {
                activity.FBButtonClick();
            }
        }

        if (zones[Networks.VK].contains(x, y)) {
            if (isVisible[Networks.VK]) {
                activity.LogoutClick(Networks.VK);
                setInvisible(Networks.VK);
            } else {
                activity.VKButtonClick();
            }
        }

        return super.onTouchEvent(event);
    }
}
