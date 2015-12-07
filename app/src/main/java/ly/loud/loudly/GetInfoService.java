package ly.loud.loudly;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.LinkedList;

import base.says.LoudlyPost;
import base.Tasks;
import base.says.Post;

public class GetInfoService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: 12/6/2015 This possible may produce leaks. Should be replaced soon
        LinkedList<Post> loudlyPosts = new LinkedList<>();
        for (Post post : MainActivity.posts) {
            if (post.getNetwork() == -1) {
                loudlyPosts.add(post);
            }
        }
        if (loudlyPosts.isEmpty()) {
            return;
        }

        Tasks.InfoGetter getter = new Tasks.InfoGetter(loudlyPosts, Loudly.getContext().getWraps());
        getter.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
