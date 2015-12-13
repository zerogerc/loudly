package ly.loud.loudly;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;

import base.SingleNetwork;
import base.Tasks;
import base.Wrap;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.InvalidTokenException;
import util.UIAction;

public class GetInfoService extends IntentService implements Tasks.GetInfoCallback {
    private static final String NAME = "GetInfoService";
    private LinkedList<Post> currentPosts;

    public GetInfoService() {
        super(NAME);
    }

    @Override
    public void infoLoaded(Post post, Info info) {
        int ind = 0;
        for (Post old : MainActivity.posts) {
            if (old.equals(post)) {
                old.setInfo(info);
                final int fixed = ind;
                MainActivity.executeOnMain(new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        MainActivity mainActivity = (MainActivity) context;
                        mainActivity.recyclerViewAdapter.notifyItemChanged(fixed);
                    }
                });
                break;
            }
            ind++;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (MainActivity.posts.isEmpty()) {
            return;
        }
        currentPosts = new LinkedList<>();
        for (Wrap w : Loudly.getContext().getWraps()) {
            try {
                currentPosts.clear();
                // Select posts only from current network
                for (Post post : MainActivity.posts) {
                    if (post.existsIn(w.networkID())) {
                        if (post instanceof LoudlyPost) {
                            post.setNetwork(w.networkID());
                        }
                        currentPosts.add(post);
                    }
                }

                w.getPostsInfo(currentPosts, this);
                Intent message = BroadcastSendingTask.makeMessage(Broadcasts.POST_GET_INFO,
                        Broadcasts.PROGRESS);
                message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                Loudly.sendLocalBroadcast(message);
            } catch (InvalidTokenException e) {
                Intent message = BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
                        Broadcasts.INVALID_TOKEN, e.getMessage());
                message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                Loudly.sendLocalBroadcast(message);
            } catch (IOException e) {
                Log.e(NAME, e.getMessage(), e);
                Loudly.sendLocalBroadcast(BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
                        Broadcasts.NETWORK_ERROR, e.getMessage()));
            }
        }
        Loudly.sendLocalBroadcast(BroadcastSendingTask.makeSuccess(Broadcasts.POST_GET_INFO));
        Loudly.getContext().startGetInfoService();  // Restarting
    }
}
