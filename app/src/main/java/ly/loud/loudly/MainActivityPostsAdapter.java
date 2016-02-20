package ly.loud.loudly;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import Loudly.LoudlyWrap;
import base.Networks;
import base.Tasks;
import base.says.LoudlyPost;
import base.says.Post;
import ly.loud.loudly.adapter.AbstractAdapter;
import ly.loud.loudly.adapter.ViewHolder;
import ly.loud.loudly.adapter.ViewHolderPost;
import util.UIAction;
import util.Utils;

public class MainActivityPostsAdapter extends AbstractAdapter<MainActivity, Post> {
    private int lastPosition = -1;

    MainActivityPostsAdapter(List<Post> posts, MainActivity activity) {
        super(posts, activity);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.e("BIND_MAIN", Integer.toString(items.size()) + ' ' + Integer.toString(position));

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
     */
    public void merge(LinkedList<? extends Post> newPosts) {
        Log.e("TAG", "merge");
        int i = 0, j = 0;
        // TODO: 12/11/2015 Make quicker with arrayLists
        while (j < newPosts.size()) {
            // while date of ith old post is greater than date of jth post in newPosts, i++
            Post right = newPosts.get(j);
            while (i < items.size() && j < newPosts.size()) {
                Post post = items.get(i);

                if (post.getDate() == right.getDate()) {
                    // Skip existing post (especially for Loudly posts)
                    j++;
                    continue;
                }
                if (post.getDate() < right.getDate()) {
                    break;
                }
                i++;
            }
            int oldJ = j;
            while (j < newPosts.size() &&
                    (i == items.size() || newPosts.get(j).getDate() > items.get(i).getDate())) {
                j++;
            }

            items.addAll(i, newPosts.subList(oldJ, j));

            int newI = i + j - oldJ;
            if (newI == items.size()) {
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
        Iterator<Post> iterator = items.listIterator();
        int ind = 0;
        while (iterator.hasNext()) {
            Post post = iterator.next();
            boolean shouldHide = true;
            if (post instanceof LoudlyPost) {
                if (!post.exists()) {
                    try {
                        // If post hasn't deleted yet, delete it
                        if (((LoudlyPost) post).getId(Networks.LOUDLY) != null) {
                            new LoudlyWrap().delete(post);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (Integer network : networks) {
                        if (post.existsIn(network)) {
                            shouldHide = false;
                        }
                    }
                }
            } else {
                if (post.exists()) {
                    shouldHide = false;
                }
            }
            if (shouldHide) {
                iterator.remove();
                final int fixed = ind;
                MainActivity.executeOnUI(new UIAction<MainActivity>() {
                    @Override
                    public void execute(MainActivity context, Object... params) {
                        context.mainActivityPostsAdapter.notifyDeletedAtPosition(fixed);
                    }
                });
            }
            ind++;
        }
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
