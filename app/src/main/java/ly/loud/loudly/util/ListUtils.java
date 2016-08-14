package ly.loud.loudly.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.functions.Func1;
import solid.collections.SolidList;

/**
 * Collection of utils for work with {@link List}
 */
public class ListUtils {
    private static ArrayList EMPTY_ARRAY_LIST;

    /**
     * Transform specified list to SolidList
     *
     * @param list A list
     * @param <T>  Type of elements
     * @return SolidList
     */
    @NonNull
    public static <T> SolidList<T> asSolidList(@NonNull List<T> list) {
        if (list instanceof SolidList) {
            return (SolidList<T>) list;
        }
        return new SolidList<T>(list);
    }

    /**
     * Put specified objects to ArrayList
     *
     * @param objects Object to put
     * @param <T>     Type of elements
     * @return ArrayList with objects
     */
    @SafeVarargs
    @NonNull
    public static <T> ArrayList<T> asArrayList(@NonNull T... objects) {
        ArrayList<T> result = new ArrayList<>();
        Collections.addAll(result, objects);
        return result;
    }

    /**
     * Transform specified list to ArrayList
     *
     * @param list A list
     * @param <T>  Type of elements
     * @return ArrayList
     */
    @NonNull
    public static <T> ArrayList<T> asArrayList(@NonNull List<T> list) {
        if (list instanceof ArrayList) {
            return (ArrayList<T>) list;
        }
        return new ArrayList<>(list);
    }

    /**
     * Get empty ArrayList
     *
     * @param <T> Type of elements
     * @return Empty ArrayList
     */
    @NonNull
    public static <T> ArrayList<T> emptyArrayList() {
        if (EMPTY_ARRAY_LIST == null) {
            EMPTY_ARRAY_LIST = new ArrayList();
        }
        //noinspection unchecked
        return (ArrayList<T>) EMPTY_ARRAY_LIST;
    }

    /**
     * Remove elements from list, using specified predicate
     *
     * @param list      List to remove elements
     * @param predicate A predicate
     * @param <T>       Type of elements in list
     */
    public static <T> void removeByPredicateInPlace(@NonNull List<T> list,
                                                    @NonNull Func1<T, Boolean> predicate) {
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            T element = iterator.next();
            if (predicate.call(element)) {
                iterator.remove();
            }
        }
    }
}
