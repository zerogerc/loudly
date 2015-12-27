package ly.loud.loudly;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import base.Networks;
import base.Tasks;
import base.Wrap;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import util.BroadcastSendingTask;
import util.Broadcasts;
import util.InvalidTokenException;
import util.ThreadStopped;
import util.UIAction;
import util.Utils;

public class GetInfoService extends IntentService implements Tasks.GetInfoCallback {
    private static final String NAME = "GetInfoService";
    private static final int NOTIFICATION_ID = 0;
    private static volatile boolean stopped;
    private Info summary;

    public GetInfoService() {
        super(NAME);
        stopped = false;
    }

    public static void stop() {
        stopped = true;
    }

    @Override
    public void infoLoaded(Post post, Info info) {
        int ind = 0;
        for (Post old : MainActivity.posts) {
            if (stopped) throw new ThreadStopped();

            if (old.equals(post)) {
                Info oldInfo;
                if (old instanceof LoudlyPost) {
                    oldInfo = ((LoudlyPost) old).getInfo(post.getNetwork());
                } else {
                    oldInfo = old.getInfo();
                }
                if (!oldInfo.equals(info)) {
                    old.setInfo(info);
                    summary.add(oldInfo.difference(info));
                    final int fixed = ind;
                    MainActivity.executeOnUI(new UIAction<MainActivity>() {
                        @Override
                        public void execute(MainActivity mainActivity, Object... params) {
                            mainActivity.recyclerViewAdapter.notifyItemChanged(fixed);
                        }
                    });
                }
                break;
            }
            ind++;
        }
    }

    private String[] makeNewInfoMessages(Info summary) {
        String message = "New";
        String longMessage = "You've got";
        if (summary.like > 0) {
            message += " likes,";
            longMessage += " " + summary.like + " new like" +
                    ((summary.like > 1) ? "s" : "") + ",";
        }
        if (summary.repost > 0) {
            message += " shares,";
            longMessage += " " + summary.repost + " new repost" +
                    ((summary.repost > 1) ? "s" : "") + ",";
        }
        if (summary.comment > 0) {
            message += " comments,";
            longMessage += " " + summary.comment + " new comment" +
                    ((summary.comment > 1) ? "s" : "") + ",";
        }
        longMessage = longMessage.substring(0, longMessage.length() - 1);
        message = message.substring(0, message.length() - 1);
        return new String[]{message, longMessage};
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        stopped = false;
        if (MainActivity.posts.isEmpty()) {
            return;
        }
        LinkedList<Post> currentPosts = new LinkedList<>();
        summary = new Info();
        final LinkedList<Integer> success = new LinkedList<>();

        for (Wrap w : Loudly.getContext().getWraps()) {
            try {
                currentPosts.clear();
                // Select posts only from current network
                for (Post post : MainActivity.posts) {
                    if (stopped) throw new ThreadStopped();
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
                success.add(w.networkID());
            } catch (InvalidTokenException e) {
                Intent message = BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
                        Broadcasts.INVALID_TOKEN, e.getMessage());
                message.putExtra(Broadcasts.NETWORK_FIELD, w.networkID());
                Loudly.sendLocalBroadcast(message);
            } catch (IOException e) {
                Log.e(NAME, e.getMessage(), e);
                Loudly.sendLocalBroadcast(BroadcastSendingTask.makeError(Broadcasts.POST_GET_INFO,
                        Broadcasts.NETWORK_ERROR, e.getMessage()));
            } catch (ThreadStopped e) {
                Loudly.sendLocalBroadcast(BroadcastSendingTask.makeSuccess(Broadcasts.POST_GET_INFO));
                return;
            }
        }

        Loudly.sendLocalBroadcast(BroadcastSendingTask.makeSuccess(Broadcasts.POST_GET_INFO));
        if (summary.hasPositiveChanges()) {
            String[] strings = makeNewInfoMessages(summary);
            if (MainActivity.aliveCopy == 0 && SettingsActivity.aliveCopy == 0) {
                Utils.makeNotification(this, strings[0], strings[1], NOTIFICATION_ID);
            } else {
                Utils.showSnackBar(strings[1]);
            }
        }

        MainActivity.executeOnUI(new UIAction<MainActivity>() {
            @Override
            public void execute(MainActivity context, Object... params) {
                context.recyclerViewAdapter.cleanUp(success);
            }
        });
        Loudly.getContext().startGetInfoService();  // Restarting
    }
}
