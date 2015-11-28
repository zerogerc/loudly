package ly.loud.loudly;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.LinkedList;

import base.Post;
import base.Tasks;
import util.database.DatabaseActions;
import util.database.DatabaseException;

public class GetInfoService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        LinkedList<Post> loudlyPosts = new LinkedList<>();
        for (Post post : Loudly.getContext().getPosts()) {
            if (post.getMainNetwork() == -1) {
                loudlyPosts.add(post);
            }
        }
        if (loudlyPosts.isEmpty()) {
            return;
        }

        Tasks.InfoGetter getter = new Tasks.InfoGetter(Loudly.getContext().getWraps());
        getter.execute(loudlyPosts.toArray(new Post[0]));
        LoadAndGetInfo task = new LoadAndGetInfo(this);
        task.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static class LoadAndGetInfo extends AsyncTask<Object, Void, Boolean> {
        Service service;

        public LoadAndGetInfo(Service service) {
            this.service = service;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                if (Loudly.getContext().getPosts().isEmpty()) {
                    DatabaseActions.loadKeys();
                    DatabaseActions.loadPosts(Loudly.getContext().getTimeInterval());
                }
            } catch (DatabaseException e) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean o) {
            if (o) {

            }
            service.stopSelf();
        }
    }
}
