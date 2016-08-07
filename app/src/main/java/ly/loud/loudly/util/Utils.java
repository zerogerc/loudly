package ly.loud.loudly.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.legacy_base.attachments.Image;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.base.entities.Person;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.base.plain.PlainPost;

public class Utils {
    private static final String TAG = "UTIL_TAG";

    public static String getDateFormatted(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("h 'hours', EEEE, d.MM", Locale.US);

        return formatter.format(cal.getTime());
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = Loudly.getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Bitmap getIconByNetwork(int network) {
        return BitmapFactory.decodeResource(Loudly.getContext().getResources(), getResourceByNetwork(network));
    }

    public static int getResourceByPost(@NonNull PlainPost post) {
        if (post instanceof SinglePost) {
            return getResourceByNetwork(((SinglePost) post).getNetwork());
        } else {
            return getResourceByNetwork(Networks.LOUDLY);
        }
    }

    public static int getResourceByNetwork(int network) {
        switch (network) {
            case Networks.LOUDLY:
                return R.mipmap.ic_launcher;
            case Networks.FB:
                return R.mipmap.ic_facebook_round;
            case Networks.TWITTER:
                return R.mipmap.ic_twitter_round;
            case Networks.INSTAGRAM:
                return R.mipmap.ic_instagram_round;
            case Networks.VK:
                return R.mipmap.ic_vk_round;
            case Networks.OK:
                return R.mipmap.ic_ok_round;
            case Networks.MAILRU:
                return R.mipmap.ic_mail_ru_round;
            default:
                return R.mipmap.ic_launcher;
        }
    }

    public static int getResourceWhiteByNetwork(int network) {
        int resource;
        switch (network) {
            case Networks.LOUDLY:
                return R.drawable.ic_loudly_white;
            case Networks.FB:
                resource = R.drawable.ic_facebook_white;
                break;
            case Networks.TWITTER:
                resource = R.drawable.ic_twitter_white;
                break;
            case Networks.INSTAGRAM:
                resource = R.drawable.ic_instagram_white;
                break;
            case Networks.VK:
                resource = R.drawable.ic_vk_white;
                break;
            case Networks.OK:
                resource = R.drawable.ic_ok_white;
                break;
            case Networks.MAILRU:
                resource = R.drawable.ic_myworld_white;
                break;
            default:
                resource = R.drawable.ic_loudly_white;
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

    @NonNull
    public static ArrayList<SinglePost> getInstances(@NonNull PlainPost post) {
        if (post instanceof SinglePost) {
            return ListUtils.asArrayList(((SinglePost) post));
        } else if (post instanceof LoudlyPost) {
             return ((LoudlyPost) post).getNetworkInstances();
        } else {
            return ListUtils.emptyArrayList();
        }
    }

    @StringRes
    public static int getNetworkTitleResourceByPost(@NonNull PlainPost post) {
        if (post instanceof SinglePost) {
            return  Networks.nameResourceOfNetwork(((SinglePost) post).getNetwork());
        } else {
            return Networks.nameResourceOfNetwork(Networks.LOUDLY);
        }
    }
}
