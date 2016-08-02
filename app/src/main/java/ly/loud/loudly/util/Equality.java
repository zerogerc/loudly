package ly.loud.loudly.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.new_base.Location;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.attachments.LoudlyImage;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import rx.functions.Func0;

import java.util.Iterator;
import java.util.List;

/**
 * Some outer equalities between classes
 *
 * @author Danil Kolikov
 */
public class Equality {
    /**
     * Check if a == b with attention to nulls
     *
     * @param a     First object (may be null)
     * @param b     Second object (may be null)
     * @param check Function, producing non-null Boolean - comparison of two non-null objects
     * @param <T>   Type of object
     * @return Is first object equal to second
     */
    private static <T> boolean equalBuilder(@Nullable T a, @Nullable T b, @NonNull Func0<Boolean> check) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return check.call();
    }

    /**
     * Null-safe equality of strings
     * @return true, of strings are equal
     */
    public static boolean equal(String a, String b) {
        return equalBuilder(a, b, () -> a.equals(b));
    }

    public static boolean equal(Uri a, Uri b) {
        return equalBuilder(a, b, () -> a.equals(b));
    }

    public static boolean equal(Link a, Link b) {
        return equalBuilder(a, b, () -> /* a.isValid() == b.isValid() && // It's a problem of architecture*/
                equal(a.get(), b.get()));
    }

    public static boolean equal(Link[] a, Link[] b) {
        return equalBuilder(a, b, () -> {
            if (a.length != b.length) {
                return false;
            }
            // 0 - link for Loudly - not checked now, because it's always null
            for (int i = 1; i < a.length; i++) {
                if (!equal(a[i], b[i])) {
                    return false;
                }
            }
            return true;
        });
    }

    public static boolean equal(Location a, Location b) {
        return equalBuilder(a, b, () -> a.equals(b));
    }

    public static boolean equal(Attachment a, Attachment b) {
        return equalBuilder(a, b, () -> {
            if ((a instanceof LoudlyImage) && (b instanceof LoudlyImage)) {
                return equal(((LoudlyImage) a).getLinks(), ((LoudlyImage) b).getLinks())
                        && equal(a.getExtra(), b.getExtra());
            }
            if ((a instanceof LoudlyImage) || (b instanceof LoudlyImage)) {
                return false;
            }
            if ((a instanceof Image) && (b instanceof Image)) {
                return equal(a.getLink(), b.getLink()) && a.getNetwork() == b.getNetwork() &&
                        equal(a.getExtra(), b.getExtra());
            }
            return false;
        });
    }

    public static boolean equal(List<Attachment> a, List<Attachment> b) {
        return equalBuilder(a, b, () -> {
            if (a.size() != b.size()) {
                return false;
            }
            Iterator<Attachment> itA = a.iterator();
            Iterator<Attachment> itB = b.iterator();
            while (itA.hasNext()) {
                if (!equal(itA.next(), itB.next())) {
                    return false;
                }
            }
            return true;
        });
    }

    public static boolean equal(Post a, Post b) {
        return equalBuilder(a, b, () -> {
            boolean equal = equal(a.getText(), b.getText()) &&
                    equal(a.getLocation(), b.getLocation()) &&
                    equal(a.getAttachments(), b.getAttachments()) &&
                    a.getDate() == b.getDate();

            if ((a instanceof LoudlyPost) && (b instanceof LoudlyPost)) {
                return equal && equal(((LoudlyPost) a).getLinks(), ((LoudlyPost) b).getLinks());
            }
            if ((a instanceof LoudlyPost) || (b instanceof LoudlyPost)) {
                return false;
            }
            return equal && a.getNetwork() == b.getNetwork() && equal(a.getLink(), b.getLink());
        });
    }
}
