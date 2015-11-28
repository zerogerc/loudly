package util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import base.Networks;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.R;

public class UtilsBundle {
    private static final String TAG = "UTIL_TAG";

    public static void hidePhoneKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int getDefaultScreenHeight() {
        WindowManager windowManager = (WindowManager) Loudly.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getDefaultScreenWidth() {
        WindowManager windowManager = (WindowManager) Loudly.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = Loudly.getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = Loudly.getContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Bitmap loadBitmap(Uri uri, int desiredWidth, int desiredHeight) {
        InputStream input = null;
        Bitmap result = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            input = Loudly.getContext().getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(input, null, options);

            if (input != null) {
                input.close();
            }

            int origWidth = options.outWidth;
            int origHeight = options.outHeight;
            int scale = 1;

            if (origWidth > origHeight) {
                scale = Math.round((float) origHeight / (float) desiredHeight);
            } else {
                scale = Math.round((float) origWidth / (float) desiredWidth);
            }

            if (scale < 1) {
                scale = 1;
            }

            options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            input = Loudly.getContext().getContentResolver().openInputStream(uri);
            result = BitmapFactory.decodeStream(input, null, options);

            if (input != null) {
                input.close();
            }
        } catch (FileNotFoundException fnfe) {
            Log.e(TAG, fnfe.getMessage());
            return null;
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        } catch (NullPointerException npe) {
            Log.e(TAG, npe.getMessage());
        }
        return result;
    }

    public static Bitmap getIconByNetwork(int network) {
        int resource;
        switch(network) {
            case Networks.FB:
                resource = R.mipmap.ic_facebook_round;
                break;
            case Networks.INSTAGRAM:
                resource = R.mipmap.ic_instagram_round;
                break;
            case Networks.MAILRU:
                resource = R.mipmap.ic_mail_ru_round;
                break;
            case Networks.TWITTER:
                resource = R.mipmap.ic_twitter_round;
                break;
            case Networks.VK:
                resource = R.mipmap.ic_vk_round;
                break;
            default:
                resource = R.mipmap.ic_ok_round;
        }
        return BitmapFactory.decodeResource(Loudly.getContext().getResources(), resource);
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
