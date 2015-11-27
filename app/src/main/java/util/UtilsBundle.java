package util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ly.loud.loudly.Loudly;

public class UtilsBundle {
    private static final String TAG = "UTIL_TAG";

    public static void hidePhoneKeypad(View view) {
        InputMethodManager inputManager = (InputMethodManager) view
                .getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        IBinder binder = view.getWindowToken();
        inputManager.hideSoftInputFromWindow(binder,
                InputMethodManager.HIDE_NOT_ALWAYS);
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
}
