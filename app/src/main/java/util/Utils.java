package util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ly.loud.loudly.Loudly;

public class Utils {
    private static final String TAG = "UTIL_TAG";

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

    public static class SavedInputStream {
        ByteArrayOutputStream bufferedStream;

        public SavedInputStream(InputStream input) {
            bufferedStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = input.read(buffer)) > -1) {
                    bufferedStream.write(buffer, 0, len);
                }
                bufferedStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public SavedInputStream(InputStream input, BackgroundAction onProgress) {
            bufferedStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            int received = 0;
            try {
                while ((len = input.read(buffer)) > -1) {
                    bufferedStream.write(buffer, 0, len);
                    received += len;
                    onProgress.execute(received);
                }
                bufferedStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bufferedStream.toByteArray());
        }
    }

    public static Bitmap makeResizedBitmap(@NonNull InputStream input,
                                           int desiredWidth, int desiredHeight,
                                           BackgroundAction... onProgress) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        SavedInputStream saved;
        if (onProgress.length == 0) {
            saved = new SavedInputStream(input);
        } else {
            saved = new SavedInputStream(input, onProgress[0]);
        }

        BitmapFactory.decodeStream(saved.getInputStream(), null, options);

        int origWidth = options.outWidth;
        int origHeight = options.outHeight;
        int scale;

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

        return BitmapFactory.decodeStream(saved.getInputStream(), null, options);
    }

    public static Bitmap loadBitmap(Uri uri, int desiredWidth, int desiredHeight) throws IOException {
        InputStream input = null;
        try {
            input = Loudly.getContext().getContentResolver().openInputStream(uri);
            if (input == null) {
                return null;
            }
            return makeResizedBitmap(input, desiredWidth, desiredHeight);
        } finally {
            closeQuietly(input);
        }
    }

    public static Bitmap downloadBitmap(String url, final BackgroundAction onProgress, int desiredWidth, int desiredHeight) throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            final int size = conn.getContentLength();
            in = conn.getInputStream();
            return makeResizedBitmap(in, desiredWidth, desiredHeight, new BackgroundAction() {
                @Override
                public void execute(Object... params) {
                    onProgress.execute(100 * (int) (params[0]) / size);
                }
            });
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            Utils.closeQuietly(in);
        }
    }


    /**
     * Close instance of Closeable without throwing exception
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception while closing: " + e.getMessage());
            }
        }
    }
}
