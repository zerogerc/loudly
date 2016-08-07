package ly.loud.loudly.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.List;

import ly.loud.loudly.base.entities.Link;
import ly.loud.loudly.base.entities.Location;
import ly.loud.loudly.base.multiple.LoudlyImage;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.util.database.entities.StoredLocation;
import rx.functions.Func0;
import rx.functions.Func2;

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
     *
     * @return true, of strings are equal
     */
    public static boolean equal(@Nullable String a, @Nullable String b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
            return a.equals(b);
        });
    }

    public static boolean equal(@Nullable Long a, @Nullable Long b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
            return a.equals(b);
        });
    }

    public static boolean equal(@Nullable Link a, @Nullable Link b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
            return equal(a.get(), b.get());
        });
    }

    public static boolean equal(@Nullable Link[] a, @Nullable Link[] b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
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

    public static boolean equal(@Nullable Location a, @Nullable Location b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
            return a.equals(b);
        });
    }

    public static boolean equal(@Nullable StoredLocation a, @Nullable StoredLocation b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
            return equal(a.getName(), b.getName()) && a.getLatitude() == b.getLatitude() &&
                    a.getLongitude() == b.getLongitude() && equal(a.getId(), b.getId());
        });
    }

    public static boolean equal(@Nullable Attachment a, @Nullable Attachment b) {
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

    private static <T> boolean equalLists(@Nullable List<T> a, @Nullable List<T> b,
                                          @NonNull Func2<T, T, Boolean> equal) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
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

    public static <T extends PlainPost> boolean equalPosts(@Nullable List<T> a, @Nullable List<T> b) {
        return equalLists(a, b, Equality::equal);
    }

    public static <T extends Attachment> boolean equal(@Nullable List<T> a, @Nullable List<T> b) {
        return equalLists(a, b, Equality::equal);
    }

    public static boolean equal(@Nullable PlainPost a, @Nullable PlainPost b) {
        return equalBuilder(a, b, () -> {
            //noinspection ConstantConditions checked in EqualBuilder
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
