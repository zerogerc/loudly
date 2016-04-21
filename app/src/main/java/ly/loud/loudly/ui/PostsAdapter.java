package ly.loud.loudly.ui;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import ly.loud.loudly.R;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.base.says.Say;
import ly.loud.loudly.ui.adapter.BaseAdapter;
import ly.loud.loudly.ui.adapter.ModifiableAdapter;
import ly.loud.loudly.ui.adapter.ViewHolder;
import ly.loud.loudly.ui.adapter.ViewHolderPost;
import ly.loud.loudly.util.Utils;

import java.util.Comparator;
import java.util.List;

public class PostsAdapter extends BaseAdapter<MainActivity, Post> implements ModifiableAdapter<Post> {
    private int lastPosition = -1;

    PostsAdapter(List<Post> posts, MainActivity activity) {
        super(posts, activity);
    }

    protected Post getPost(int position) {
        return items.get(position);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Post post = getPost(position);

        if (holder instanceof ViewHolderPost) {
            ViewHolderPost postHolder = (ViewHolderPost) holder;

            postHolder.setLikesOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, post, Tasks.LIKES);
                }
            });

            postHolder.setCommentsOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showComments(activity, post);
                }
            });

            postHolder.setRepostsOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, post, Tasks.SHARES);
                }
            });

            if (Networks.makeWrap(post.getNetwork()).getDescription().canDelete()) {
                postHolder.showDeleteButton();
                postHolder.setDeleteOnClick(makeDeleteClickListener(post));
            } else {
                postHolder.hideDeleteButton();
            }
        }

        holder.refresh(post);
        setItemsAppearingAnimation(holder.itemView, position);
    }

    public void notifyItemDeletedAtPosition(int pos) {
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
                                new Tasks.PostDeleter(post, Loudly.getContext().getWraps()).
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
        }
    }

    private static <T> int linearSearch(List<? extends T> items, T element, Comparator<? super T> comparator) {
        int pos = 0;
        for (T e : items) {
            int cmp = comparator.compare(e, element);
            if (cmp < 0) {
                pos++;
                continue;
            }
            break;
        }
        return pos;
    }

    @Override
    public void update(List<? extends Post> updated) {
        for (Post p : updated) {
            int pos = linearSearch(items, p, Say.FEED_ORDER);
            notifyItemChanged(pos);
        }
    }

    @Override
    public void insert(List<? extends Post> inserted) {
        notifyDataSetChanged();
        // ToDo: Commented code doesn't work because of internal error of RecyclerView. Maybe it can be fixed
//        for (Post p : inserted) {
//            int pos = linearSearch(items, p, Say.FEED_ORDER);
//            if (pos < items.size() - 1) {
//                notifyItemInserted(pos);
//            } else {
//                notifyDataSetChanged();
//            }
//        }
    }

    @Override
    public void delete(List<? extends Post> deleted) {
        for (Post p : deleted) {
            int pos = linearSearch(items, p, Say.FEED_ORDER);
            notifyItemDeletedAtPosition(pos);
        }
    }
}
