package ly.loud.loudly.util;

import android.support.annotation.NonNull;
import com.android.internal.util.Predicate;

import java.util.*;

/**
 * @author Danil Kolikov
 */
public class FilteredListView<E> implements List<E> {
    private Predicate<E> predicate;
    private List<? extends E> list;

    public FilteredListView(List<? extends E> list, Predicate<E> predicate) {
        this.predicate = predicate;
        this.list = list;
    }

    @Override
    public int size() {
        int size = 0;
        for (E t : this) {
            size++;
        }
        return size;
    }

    @Override
    public boolean contains(Object object) {
        for (E t : this) {
            if (t.equals(object)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object o : collection) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public E get(int location) {
        int pos = 0;
        for (E t : this) {
            if (pos == location) {
                return t;
            }
            pos++;
        }
        return null;
    }


    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public int indexOf(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }


    @Override
    public int lastIndexOf(Object object) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public List<E> subList(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int location, E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }


    @Override
    public ListIterator<E> listIterator() {
        return new ListIterator<E>() {
            private ListIterator<? extends E> iterator = list.listIterator();
            E prev, next;

            @Override
            public void add(E object) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (predicate.apply(next)) {
                        return true;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public E next() {
                if (next == null) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                E tmp = next;
                next = null;
                prev = null;
                return tmp;
            }

            @Override
            public boolean hasPrevious() {
                while (iterator.hasPrevious()) {
                    prev = iterator.previous();
                    if (predicate.apply(prev)) {
                        return true;
                    }
                }
                prev = null;
                return false;
            }

            @Override
            public int nextIndex() {
                throw new UnsupportedOperationException();
            }

            @Override
            public E previous() {
                if (prev == null) {
                    if (!hasPrevious()) {
                        throw new NoSuchElementException();
                    }
                }
                E tmp = prev;
                prev = null;
                next = null;
                return tmp;
            }

            @Override
            public int previousIndex() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(E object) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(int location, E object) {
        throw new UnsupportedOperationException();
    }

}
