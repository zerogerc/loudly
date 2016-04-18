package ly.loud.loudly.ui;

import android.util.Log;
import android.util.Pair;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.SingleNetwork;
import ly.loud.loudly.base.says.Info;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.base.says.Say;
import ly.loud.loudly.networks.Loudly.LoudlyWrap;
import ly.loud.loudly.ui.adapter.ModifiableAdapter;

import java.io.IOException;
import java.util.*;

/**
 * @author Danil Kolikov
 */
public class PostsHolder {
    private List<Post> posts;
    private Map<Pair<Integer, String>, LoudlyPost> hiddenPosts;
    private ModifiableAdapter<Post> adapter;

    public PostsHolder() {
        this.posts = new ArrayList<>();
        hiddenPosts = new HashMap<>();
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setAdapter(ModifiableAdapter<Post> adapter) {
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

    private void addLinks(List<? extends Post> posts) {
        for (Post p : posts) {
            LoudlyPost loudlyPost = (LoudlyPost)p;
            for (int i = 1; i < Networks.NETWORK_COUNT; i++) {
                if (loudlyPost.getLink(i) != null) {
                    hiddenPosts.put(new Pair<>(i, loudlyPost.getLink(i).get()), loudlyPost);
                }
            }
        }
    }

    private void addWithoutDuplicates(List<? extends Post> from, List<Post> dest) {
        for (Post p : from) {
            if (p instanceof LoudlyPost) {
                dest.add(p);
            } else {
                LoudlyPost found = hiddenPosts.get(new Pair<>(p.getNetwork(), p.getLink().get()));
                if (found == null) {
                    dest.add(p);
                } else {
                    found.getLink().setValid(true);
                    found.getLink(p.getNetwork()).setValid(true);
                    found.setInfo(p.getNetwork(), p.getInfo());
                }
            }
        }
    }

    public void merge(List<? extends Post> newPosts, final int network) {
        if (network == Networks.LOUDLY) {
            addLinks(newPosts);
        }
        Pair<List<Post>, List<Post>> merged = merge(posts, newPosts, Say.FEED_ORDER);
        posts.clear();
        addWithoutDuplicates(merged.first, posts);
        adapter.insert(merged.second);
        adapter.update(merged.second);
    }

    /**
     * Remove from the feed posts, removed from other network<b>
     * If found loudly posts, that was removed from every network, delete it from DB <b>
     * Otherwise, remove from feed
     *
     * @param networks list of wraps, from where posts were loaded
     */
    public void cleanUp(List<Integer> networks) {
        ArrayList<Integer> notLoaded = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (!(networks.contains(i))) {
                notLoaded.add(i);
            }
        }

        Iterator<Post> iterator = posts.iterator();
        List<Post> deleted = new ArrayList<>();

        while (iterator.hasNext()) {
            Post post = iterator.next();

            // Post is valid if it exists in any network
            boolean valid = false;
            for (int id : networks) {
                SingleNetwork instance = post.getNetworkInstance(id);
                if (instance != null) {
                    if (instance.exists()) {
                        valid = true;
                    }
                    if (instance.getLink() != null && !instance.getLink().isValid()) {
                        instance.setLink(null);
                        if (!deleted.contains(post)) {
                            deleted.add(post);
                        }
                    }
                }
            }
            if (!valid) {
                // Check if post has links in network, from which posts wasn't loaded
                boolean existsSomewhere = false;
                for (int id : notLoaded) {
                    SingleNetwork instance = post.getNetworkInstance(id);
                    if (instance != null && instance.getLink() != null) {
                        existsSomewhere = true;
                        break;
                    }
                }

                // If post doesn't exist in any network
                if (!existsSomewhere) {
                    if (!deleted.contains(post)) {
                        deleted.add(post);
                    }
                    // Hide post
                    iterator.remove();
                    if (post instanceof LoudlyPost) {
                        // And delete from DB
                        try {
                            new LoudlyWrap().delete(post);
                        } catch (IOException e) {
                            Log.e("cleanUp", "can't delete post from DB", e);
                        }

                    }
                }
            }
        }
        adapter.delete(deleted);
    }

    public Info updateInfo(List<Pair<Post, Info>> pairs, int network) {
        Iterator<Post> iterator = posts.iterator();
        Info summary = new Info();
        List<Post> updated = new ArrayList<>();

        for (Pair<Post, Info> pair : pairs) {
            while (iterator.hasNext()) {
                Post post = (Post) iterator.next().getNetworkInstance(network);
                if (post != null && post.equals(pair.first.getNetworkInstance(network))) {
                    Info oldInfo = post.getInfo();
                    if (pair.second != null && !oldInfo.equals(pair.second)) {
                        summary.add(pair.second.subtract(post.getInfo()));
                        post.setInfo(pair.second);

                        updated.add(post);
                    }
                    break;
                }
            }
        }
        adapter.update(updated);
        return summary;
    }
}
