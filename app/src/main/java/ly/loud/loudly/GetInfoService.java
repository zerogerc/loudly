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

public class GetInfoService extends IntentService implements Tasks.GetInfoCallback {
    private static final String NAME = "GetInfoService";
    private NotificationManager notificationManager;
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
                    MainActivity.executeOnUI(new UIAction() {
                        @Override
                        public void execute(Context context, Object... params) {
                            MainActivity mainActivity = (MainActivity) context;
                            mainActivity.recyclerViewAdapter.notifyItemChanged(fixed);
                        }
                    });
                }
                break;
            }
            ind++;
        }
    }

    @Override
    public void foundDeletedPost(Post post) {
        int ind = 0;
        Iterator<Post> iterator = MainActivity.posts.listIterator();
        while (iterator.hasNext()) {
            if (stopped) throw new ThreadStopped();

            Post old = iterator.next();
            if (old.equals(post)) {
                old.setId(null);
                if (old instanceof LoudlyPost) {
                    boolean visible = false;
                    for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
                        if (post.existsIn(i)) {
                            visible = true;
                            break;
                        }
                    }
                    if (visible) break;
                }
                iterator.remove();
                final int fixed = ind;
                MainActivity.executeOnUI(new UIAction() {
                    @Override
                    public void execute(Context context, Object... params) {
                        MainActivity mainActivity = (MainActivity) context;
                        mainActivity.recyclerViewAdapter.notifyDeletedAtPosition(fixed);
                    }
                });
                break;
            }
            ind++;
        }
    }

    private void showSnackBar(Info summary) {
        final String message = makeNewInfoMessages(summary)[1];
        MainActivity.executeOnUI(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                MainActivity mainActivity = (MainActivity) context;
                Snackbar.make(mainActivity.findViewById(R.id.main_layout),
                        message, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
        SettingsActivity.executeOnUI(new UIAction() {
            @Override
            public void execute(Context context, Object... params) {
                SettingsActivity settingsActivity = (SettingsActivity) context;
                Snackbar.make(settingsActivity.findViewById(R.id.settings_parent_layout),
                        message, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void makeNotification(Info summary) {
        String[] messages = makeNewInfoMessages(summary);
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(messages[0])
                .setContentText(messages[1])
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationCompat.setContentIntent(resultPendingIntent);
        // mId allows you to update the notification later on.
        notificationManager.notify(NOTIFICATION_ID, notificationCompat.build());
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
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        stopped = false;
        if (MainActivity.posts.isEmpty()) {
            return;
        }
        LinkedList<Post> currentPosts = new LinkedList<>();
        summary = new Info();
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
            if (MainActivity.aliveCopy == 0 && SettingsActivity.aliveCopy == 0) {
                makeNotification(summary);
            } else {
                showSnackBar(summary);
            }
        }

        Loudly.getContext().startGetInfoService();  // Restarting
    }
}
