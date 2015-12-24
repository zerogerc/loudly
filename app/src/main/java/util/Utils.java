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
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import base.Networks;
import base.Person;
import base.attachments.Image;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.R;

public class Utils {
    private static final String TAG = "UTIL_TAG";

    public static int dipToPixels(float dipValue) {
        DisplayMetrics metrics = Loudly.getContext().getResources().getDisplayMetrics();
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics));
    }

    public static int pixelsToDip(float pixel)
    {
        float scale = Loudly.getContext().getResources().getDisplayMetrics().density;
        return ((int)(pixel * scale + 0.5f));
    }

    public static String getDateFormatted(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date * 1000);
        return cal.get(Calendar.DAY_OF_MONTH) + "." + +(cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR)
                + " around " + cal.get(Calendar.HOUR_OF_DAY) + " hours";
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

    public static Bitmap getIconByNetwork(int network) {
        return BitmapFactory.decodeResource(Loudly.getContext().getResources(), getResourceByNetwork(network));
    }

    public static int getResourceByNetwork(int network) {
        int resource;
        switch (network) {
            case Networks.FB:
                resource = R.mipmap.ic_facebook_round;
                break;
            case Networks.TWITTER:
                resource = R.mipmap.ic_twitter_round;
                break;
            case Networks.INSTAGRAM:
                resource = R.mipmap.ic_instagram_round;
                break;
            case Networks.VK:
                resource = R.mipmap.ic_vk_round;
                break;
            case Networks.OK:
                resource = R.mipmap.ic_ok_round;
                break;
            case Networks.MAILRU:
                resource = R.mipmap.ic_mail_ru_round;
                break;
            default:
                resource = R.mipmap.ic_launcher;
        }
        return resource;
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

    public static InputStream openStream(Image image) throws IOException {
//        if (image.isLocal()) {
//            return image.getContent();
//        } else {
            HttpURLConnection conn = (HttpURLConnection) new URL(image.getExtra()).openConnection();
            return conn.getInputStream();
//        }
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

    public static Point resolveImageSize(Image image) {
        InputStream inputStream = null;
        int imageWidth = 0;
        int imageHeight = 0;
        try {
            inputStream = Utils.openStream(image);
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
            imageWidth = bitmapOptions.outWidth;
            imageHeight = bitmapOptions.outHeight;
        } catch (IOException ioe) {
            Log.e("REC_VIEW", "IOException");
            ;
            // TODO
        } finally {
            Utils.closeQuietly(inputStream);
        }

        return new Point(imageWidth, imageHeight);
    }

    public static void clearCookies(String domain) {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookiestring = cookieManager.getCookie(domain);
        String[] cookies =  cookiestring.split(";");
        for (int i=0; i<cookies.length; i++) {
            String[] cookieparts = cookies[i].split("=");
            cookieManager.setCookie(domain, cookieparts[0].trim()+"=; Expires=Wed, 31 Dec 2025 23:59:59 GMT");
        }
    }

    public static void loadAvatar(final Person person, final ImageView icon) {
        if (person.getPhotoUrl() != null) {
            Glide.with(Loudly.getContext())
                    .load(person.getPhotoUrl())
                    .asBitmap()
                    .override(Utils.dpToPx(48), Utils.dpToPx(48))
                    .fitCenter()
                    .into(new BitmapImageViewTarget(icon) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(Loudly.getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            icon.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            Glide.with(Loudly.getContext())
                    .load(R.mipmap.ic_launcher)
                    .override(Utils.dpToPx(48), Utils.dpToPx(48))
                    .fitCenter()
                    .into(icon);
        }
    }

    public static void loadName(Person person, TextView name) {
        if (person.getFirstName() != null && person.getLastName() != null) {
            String text = person.getFirstName() + " " + person.getLastName();
            name.setText(text);
        } else {
            name.setText("");
        }
    }
}
