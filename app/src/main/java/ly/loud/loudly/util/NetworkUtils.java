package ly.loud.loudly.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.single.SinglePost;

public class NetworkUtils {
    public static class DividedList {
        public final List<SinglePost> cached;
        public final TimeInterval before, after;

        public DividedList(List<SinglePost> cached, TimeInterval before, TimeInterval after) {
            this.cached = cached;
            this.before = before;
            this.after = after;
        }
    }

    @NonNull
    public static DividedList divideListOfCachedPosts(@NonNull List<SinglePost> cached,
                                                      @NonNull TimeInterval timeInterval) {
        long firstLoadedPostDate = timeInterval.to;
        long lastLoadedPostDate = timeInterval.from;
        List<SinglePost> fromCache = new ArrayList<>();

        for (SinglePost post : cached) {
            if (timeInterval.contains(post.getDate())) {
                firstLoadedPostDate = Math.min(post.getDate(), firstLoadedPostDate);
                lastLoadedPostDate = Math.max(post.getDate(), lastLoadedPostDate);
                fromCache.add(post);
            }
        }
        return new DividedList(fromCache, new TimeInterval(timeInterval.from, firstLoadedPostDate),
                new TimeInterval(lastLoadedPostDate, timeInterval.to));
    }
}
