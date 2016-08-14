package ly.loud.loudly.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ly.loud.loudly.base.plain.PlainPost;

public class NetworkUtils {
    public static class DividedList<T extends PlainPost> {
        @NonNull
        public final List<T> cached;
        @NonNull
        public final TimeInterval before, after;

        public DividedList(@NonNull List<T> cached,
                           @NonNull TimeInterval before,
                           @NonNull TimeInterval after) {
            this.cached = cached;
            this.before = before;
            this.after = after;
        }
    }

    @NonNull
    public static <T extends PlainPost> DividedList<T> divideListOfCachedPosts(
            @NonNull List<T> cached,
            @NonNull TimeInterval timeInterval) {
        long firstLoadedPostDate = timeInterval.to;
        long lastLoadedPostDate = timeInterval.from;
        List<T> fromCache = new ArrayList<>();

        for (T post : cached) {
            if (timeInterval.contains(post.getDate())) {
                firstLoadedPostDate = Math.min(post.getDate(), firstLoadedPostDate);
                lastLoadedPostDate = Math.max(post.getDate(), lastLoadedPostDate);
                fromCache.add(post);
            }
        }
        return new DividedList<>(fromCache, new TimeInterval(timeInterval.from, firstLoadedPostDate),
                new TimeInterval(lastLoadedPostDate, timeInterval.to));
    }
}
