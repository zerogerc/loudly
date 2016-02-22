package ly.loud.loudly;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import Loudly.LoudlyWrap;
import base.Networks;
import base.SingleNetwork;
import base.Tasks;
import base.says.Info;
import base.says.LoudlyPost;
import base.says.Post;
import ly.loud.loudly.adapter.BaseAdapter;
import ly.loud.loudly.adapter.ViewHolder;
import ly.loud.loudly.adapter.ViewHolderPost;
import util.UIAction;
import util.Utils;

public class MainActivityPostsAdapter extends BaseAdapter<MainActivity, Post> {
    private int lastPosition = -1;

    MainActivityPostsAdapter(List<Post> posts, MainActivity activity) {
        super(posts, activity);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Post post = items.get(position);

        if (holder instanceof ViewHolderPost) {
            ((ViewHolderPost) holder).setLikesOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, post, Tasks.LIKES);
                }
            });

            ((ViewHolderPost) holder).setCommentsOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showComments(activity, post);
                }
            });

            ((ViewHolderPost) holder).setRepostsOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, post, Tasks.SHARES);
                }
            });

            ((ViewHolderPost) holder).setDeleteOnClick(makeDeleteClickListener(post));
        }

        holder.refresh(post);
        setItemsAppearingAnimation(holder.itemView, position);
    }

    public void notifyPostChanged(Post post) {
        int pos = 0;
        for (Post p : items) {
            if (p.equals(post)) {
                notifyItemChanged(pos);
            }
            pos++;
        }
    }

    /**
     * Merge two lists of posts into one according to date they were ulpoaded
     *
     * @param newPosts
     * @param network
     */
    public void merge(List<? extends Post> newPosts, int network) {
        Log.e("TAG", "merge");
        int i = 0, j = 0;
        // TODO: 12/11/2015 Make quicker with arrayLists
        // j iterates over newPosts, i over oldPosts
        rightLoop: while (j < newPosts.size()) {
            // while date of ith old post is greater than date of jth post in newPosts, i++
            Post right = newPosts.get(j);
            SingleNetwork rightNetworkInstance = right.getNetworkInstance(network);

            while (i < items.size() && j < newPosts.size()) {
                Post post = items.get(i);
                SingleNetwork postNetworkInstance = post.getNetworkInstance(network);

                // Skip posts with the same ID and mark them as loaded
                if (rightNetworkInstance != null && postNetworkInstance != null &&
                        rightNetworkInstance.getLink().equals(postNetworkInstance.getLink())) {
                    if (post instanceof LoudlyPost) {
                        // LoudlyPost is loaded, mark it as loaded and update info
                        ((LoudlyPost) post).getLink(Networks.LOUDLY).setValid(true);
                    }
                    postNetworkInstance.getLink().setValid(true);

                    // Update info if necessary
                    if (!postNetworkInstance.getInfo().equals(rightNetworkInstance.getInfo())) {
                        postNetworkInstance.setInfo(rightNetworkInstance.getInfo());
                        notifyItemChanged(i);
                    }
                    j++;
                    i++;
                    continue rightLoop;
                }

                if (post.getDate() < right.getDate()) {
                    break;
                }
                i++;
            }

            Post tmp = i < items.size() ? items.get(i) : null;    // Insert after this
            int oldJ = j;
            while (j < newPosts.size() &&
                    (i == items.size() || newPosts.get(j).getDate() > tmp.getDate())) {
                j++;
            }

            items.addAll(i, newPosts.subList(oldJ, j));

            int newI = i + j - oldJ;
            if (newI == items.size()) {
                // We're unlucky, I don't know why, but here we should reset all viewholders
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(i, j - oldJ);
            }
            i = newI;
        }
    }

    /**
     * Remove from the feed posts, removed from other network<b>
     * If found loudly posts, that was removed from every network, delete it from DB <b>
     * Otherwise, remove from feed
     *
     * @param networks list of wraps, from where posts were loaded
     */
    public void cleanUp(List<Integer> networks) {
        Log.e("TAG", "clean");
        ArrayList<Integer> notLoaded = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (!(networks.contains(i))) {
                notLoaded.add(i);
            }
        }

        Iterator<Post> iterator = items.listIterator();
        int ind = 0;
        while (iterator.hasNext()) {
            Post post = iterator.next();

            // Post is valid if it exists in any network
            boolean valid = false;
            for (int id : networks) {
                SingleNetwork instance = post.getNetworkInstance(id);
                if (instance != null && post.getNetworkInstance(id).exists()) {
                    valid = true;
                    break;
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
                    // Hide post
                    iterator.remove();
                    notifyDeletedAtPosition(ind);
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

            ind++;
        }
    }

    public Info updateInfo(List<Pair<Post, Info>> pairs, int network) {
        Iterator<Post> iterator = MainActivity.posts.iterator();
        int curPost = 0;
        Info summary = new Info();
        for (Pair<Post, Info> pair : pairs) {
            while (iterator.hasNext()) {
                Post post = (Post) iterator.next().getNetworkInstance(network);
                curPost++;
                if (post != null && post.equals(pair.first.getNetworkInstance(network))) {
                    Info oldInfo = post.getInfo();
                    if (!oldInfo.equals(pair.second)) {
                        summary.add(pair.second.subtract(post.getInfo()));
                        post.setInfo(pair.second);

                        notifyItemChanged(curPost - 1);
                    }
                    break;
                }
            }
        }
        return summary;
    }

    public void notifyDeletedAtPosition(int pos) {
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, items.size());
    }

    private View.OnClickListener makeDeleteClickListener(final Post post) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Delete post?")
                        .setMessage("Do you want to delete this post from all networks?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Loudly.getContext().stopGetInfoService();
                                activity.floatingActionButton.show();
                                MainActivity.receivers[MainActivity.POST_DELETE_RECEIVER] =
                                        new MainActivity.PostDeleteReceiver(activity);
                                new Tasks.PostDeleter(post, MainActivity.posts, Loudly.getContext().getWraps()).
                                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        };
    }

    private void setItemsAppearingAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(viewToAnimate, "translationY", Utils.getDefaultScreenHeight(), 0);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
//            Animation animation = AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left);
//            viewToAnimate.startAnimation(animation);
            animator.start();
            lastPosition = position;
            Log.e("POS", Integer.toString(lastPosition));
        }
    }
}
