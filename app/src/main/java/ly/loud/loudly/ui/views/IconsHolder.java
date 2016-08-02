package ly.loud.loudly.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import ly.loud.loudly.new_base.Networks;
import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.util.UIAction;
import ly.loud.loudly.util.Utils;

public class IconsHolder extends View {
    public static int SHOW_ALL = 1;
    public static int SHOW_ONLY_AVAILABLE = 2;
    public static int SHOW_ONLY_POSTABLE = 3;

    private Context context;
    private int iconWidth;
    private int iconHeight;
    private int marginTopBottom = 0;
    private int columns = 0;
    private int rows = 0;
    private int margin = 0;
    private Rect[] zones = new Rect[Networks.NETWORK_COUNT];
    private boolean[] isVisible = new boolean[Networks.NETWORK_COUNT];
    private boolean[] available = new boolean[Networks.NETWORK_COUNT];
    private UIAction colorItemClick = null;
    private UIAction grayItemClick = null;

    private int availeableAmount;

    public void prepareViewForPost(LoudlyPost post) {
//        this.mode = SHOW_ONLY_POSTABLE;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            Wrap wrap = Networks.makeWrap(i);
            if (wrap != null && wrap.checkPost(post) == null) {
                    available[i] = true;
            } else {
                available[i] = false;
            }
        }

        availeableAmount = 0;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (available[i]) {
                availeableAmount++;
            }
        }
        invalidate();
    }

    public void prepareView(int mode) {
//        this.mode = mode;
        if (mode == SHOW_ALL) {
            for (int i = 1; i < Networks.NETWORK_COUNT; i++) {
                available[i] = true;
            }

        } else {
            for (int i = 1; i < Networks.NETWORK_COUNT; i++) {
                if (Loudly.getContext().getKeyKeeper(i) != null && Networks.makeWrap(i).getDescription().canPost()) {
                    available[i] = true;
                    isVisible[i] = true;
                } else {
                    available[i] = false;
                    isVisible[i] = false;
                }
            }
        }

        availeableAmount = 0;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (available[i]) {
                availeableAmount++;
            }
        }
        requestLayout();
        invalidate();
    }

    public IconsHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        for (int network = 1; network < Networks.NETWORK_COUNT; network++) {
            if (Loudly.getContext().getKeyKeeper(network) != null) {
                available[network] = true;
                isVisible[network] = true;
            } else {
                available[network] = false;
                isVisible[network] = false;
            }
        }

        Bitmap bitmap = Utils.getIconByNetwork(Networks.FB);
        iconHeight = bitmap.getHeight();
        iconWidth = bitmap.getWidth();
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        columns = Math.min(availeableAmount, parentWidth / iconWidth);
        if (columns != 0) {
            rows = availeableAmount / columns + 1;
            if (availeableAmount % columns == 0) {
                rows--;
            }
        } else {
            rows = 0;
        }
        margin = (parentWidth - (iconWidth) * columns) / (columns + 1);
        marginTopBottom = Utils.dpToPx(8);

        setMeasuredDimension(parentWidth, iconHeight * rows + margin * (rows - 1) + marginTopBottom * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cur_w = margin;
        int cur_h = marginTopBottom;
        int picNumber = 0;
        for (int network = 1; network < Networks.NETWORK_COUNT; network++) {
            if (!available[network]) continue;
            Bitmap bitmap = null;
            Log.d("ICONS", "image");
            if (isVisible[network]) {
                bitmap = Utils.getIconByNetwork(network);
            } else {
                Bitmap bm = Utils.getIconByNetwork(network);
                bitmap = Utils.toGrayscale(bm);
            }
            zones[network] = new Rect(cur_w, cur_h, cur_w + iconWidth, cur_h + iconHeight);
            canvas.drawBitmap(bitmap, cur_w, cur_h, null);

            cur_w += iconWidth + margin;
            if ((columns != 0) && ((picNumber + 1) % columns == 0)) {
                cur_h += iconHeight + margin;
                cur_w = margin;
            }
            picNumber++;
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

    public void setColorItemsClick(UIAction action) {
        this.colorItemClick = action;
    }

    public void setGrayItemClick(UIAction action) {
        this.grayItemClick = action;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        int network = -1;
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (!available[i]) continue;;
            if (zones[i].contains(x,y)) {
                network = i;
            }
        }

        if (network != -1 && available[network]) {
            if (isVisible[network] && colorItemClick != null) {
                colorItemClick.execute(context, network);
            } else if (grayItemClick != null){
                grayItemClick.execute(context, network);
            }
        }

        return super.onTouchEvent(event);
    }
}
