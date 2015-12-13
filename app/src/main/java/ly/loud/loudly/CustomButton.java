package ly.loud.loudly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.Button;

import util.Utils;

/**
 * Created by ZeRoGerc on 14.12.15.
 */
public class CustomButton extends Button
{
    Rect r = new Rect();
    private Drawable buttonIcon = null;

    public CustomButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public CustomButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomButton(Context context)
    {
        super(context);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);


        Drawable icon = getButtonIcon();
        if(icon != null)
        {
            int drawableHeight = icon.getIntrinsicHeight();
            int drawableWidth = icon.getIntrinsicWidth();
            if(icon instanceof BitmapDrawable)
            {
                Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
                drawableWidth = Utils.dipToPixels(bitmap.getWidth());
                drawableHeight = Utils.dipToPixels(bitmap.getHeight());
            }
            else
            {
                drawableWidth = Utils.dipToPixels(icon.getIntrinsicWidth());
                drawableHeight = Utils.dipToPixels(icon.getIntrinsicHeight());
            }
            float left = (getWidth() - drawableWidth) / 2;

            int height = getHeight();
            int top = (height - drawableHeight) /2;
            int right = (int) (left + drawableWidth);
            int bottom = top + drawableHeight;
            r.set((int) left, top, right, bottom);
            icon.setBounds(r);
            icon.draw(canvas);
        }
    }

    private Drawable getButtonIcon()
    {
        return buttonIcon;
    }

    public void setButtonIcon(Drawable buttonIcon)
    {
        this.buttonIcon = buttonIcon;
        Bitmap b = ((BitmapDrawable)buttonIcon).getBitmap();
        Bitmap bitmap = Bitmap.createScaledBitmap(b, Utils.dpToPx(12), Utils.dpToPx(12), true);
        this.buttonIcon = new BitmapDrawable(getResources(), bitmap);
        int px = Utils.dpToPx(12);
        this.buttonIcon.setBounds(0, 0, px, px);
        DrawableCompat.setTint(this.buttonIcon, Color.GRAY);
    }
}
