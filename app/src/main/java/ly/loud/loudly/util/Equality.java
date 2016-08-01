package ly.loud.loudly.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.*;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.new_base.plain.PlainPost;
import rx.functions.Func0;
import rx.functions.Func2;

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
                return equal(((LoudlyImage) a).getNetworkInstances(), ((LoudlyImage) b).getNetworkInstances())
                        && equal(a.getExtra(), b.getExtra());
            }
            if ((a instanceof LoudlyImage) || (b instanceof LoudlyImage)) {
                return false;
            }
            if ((a instanceof SingleImage) && (b instanceof SingleImage)) {
                return equal(((SingleImage) a).getLink(), ((SingleImage) b).getLink())
                        && ((SingleImage) a).getNetwork() == ((SingleImage) b).getNetwork() &&
                        equal(a.getExtra(), b.getExtra());
            }
            return false;
        });
    }

    private static <T> boolean equalLists(List<T> a, List<T> b, Func2<T, T, Boolean> equal) {
        return equalBuilder(a, b, () -> {
            if (a.size() != b.size()) {
                return false;
            }
            Iterator<T> itA = a.iterator();
            Iterator<T> itB = b.iterator();
            while (itA.hasNext()) {
                if (!equal.call(itA.next(), itB.next())) {
                    return false;
                }
            }
            return true;
        });
    }
    public static <T extends PlainPost> boolean equalPosts(List<T> a, List<T> b) {
        return equalLists(a, b, Equality::equal);
    }

    public static <T extends Attachment> boolean equal(List<T> a, List<T> b) {
        return equalLists(a, b, Equality::equal);
    }

    public static boolean equal(PlainPost a, PlainPost b) {
        return equalBuilder(a, b, () -> {
            boolean equal = equal(a.getText(), b.getText()) &&
                    equal(a.getLocation(), b.getLocation()) &&
                    equal(a.getAttachments(), b.getAttachments()) &&
                    a.getDate() == b.getDate();

            if ((a instanceof LoudlyPost) && (b instanceof LoudlyPost)) {
                return equal && equalPosts(((LoudlyPost) a).getNetworkInstances(),
                        ((LoudlyPost) b).getNetworkInstances());
            }
            if ((a instanceof LoudlyPost) || (b instanceof LoudlyPost)) {
                return false;
            }
            return equal && ((SinglePost) a).getNetwork() == ((SinglePost) b).getNetwork()
                    && equal(((SinglePost) a).getLink(), ((SinglePost) b).getLink());
        });
    }
}
