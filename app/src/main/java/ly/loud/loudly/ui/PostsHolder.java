package ly.loud.loudly.ui;

import android.util.Pair;
import ly.loud.loudly.new_base.Info;
import ly.loud.loudly.new_base.LoudlyPost;
import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.ui.adapter.ModifiableAdapter;

import java.util.*;

/**
 * @author Danil Kolikov
 */
public class PostsHolder {
    private List<PlainPost> posts;
    private Map<Pair<Integer, String>, LoudlyPost> hiddenPosts;
    private ModifiableAdapter<PlainPost> adapter;

    public PostsHolder() {
        this.posts = new ArrayList<>();
        hiddenPosts = new HashMap<>();
    }

    public List<PlainPost> getPosts() {
        return posts;
    }

    public void setAdapter(ModifiableAdapter<PlainPost> adapter) {
        this.adapter = adapter;
    }

    private <T> List<T> addLower(ListIterator<? extends T> iterator, ListIterator<? extends T> bound,
                                 Comparator<? super T> cmp) {
        List<T> result = new ArrayList<>();
        if (!bound.hasNext()) {
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
            return result;
        }
        T bnd = bound.next();
        while (iterator.hasNext()) {
            T element = iterator.next();
            int compare = cmp.compare(element, bnd);
            if (compare < 0) {
                result.add(element);
                continue;
            }
            if (compare > 0) {
                iterator.previous();
                break;
            }
        }
        bound.previous();
        return result;
    }

    private <T> Pair<List<T>, List<T>> merge(List<? extends T> first, List<? extends T> second, Comparator<? super T> cmp) {
        List<T> result = new LinkedList<>();
        List<T> added = new ArrayList<>();
        ListIterator<? extends T> firstIterator = first.listIterator();
        ListIterator<? extends T> secondIterator = second.listIterator();
        List<T> inserted;
        while (firstIterator.hasNext() || secondIterator.hasNext()) {
            inserted = addLower(firstIterator, secondIterator, cmp);
            result.addAll(inserted);
            inserted = addLower(secondIterator, firstIterator, cmp);
            result.addAll(inserted);
            added.addAll(inserted);
        }
        return new Pair<>(result, added);
    }

    private void addWithoutDuplicates(List<? extends PlainPost> from, List<PlainPost> dest) {
    }

    public void merge(List<? extends PlainPost> newPosts, final int network) {
        // ToDo: here was merge
    }

    /**
     * Remove from the feed posts, removed from other network<b>
     * If found loudly posts, that was removed from every network, delete it from DB <b>
     * Otherwise, remove from feed
     *
     * @param networks list of wraps, from where posts were loaded
     */
    // ToDo: Make more stable
    public void cleanUp(List<Integer> networks, boolean deleteFromDB) {
    }

    public Info updateInfo(List<Pair<PlainPost, Info>> pairs, int network) {
        return new Info();
    }

    public void clear() {
        if (!posts.isEmpty()) {
            List<PlainPost> old = posts;
            posts = new ArrayList<>();
            adapter.delete(old);
        }
    }
}
