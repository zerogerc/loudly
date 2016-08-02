package ly.loud.loudly.ui;

import android.animation.ObjectAnimator;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.List;

import ly.loud.loudly.new_base.plain.PlainPost;
import ly.loud.loudly.ui.adapter.BaseAdapter;
import ly.loud.loudly.ui.adapter.ViewHolder;
import ly.loud.loudly.ui.adapter.ViewHolderPost;
import ly.loud.loudly.util.Utils;

import static ly.loud.loudly.application.models.GetterModel.LIKES;
import static ly.loud.loudly.application.models.GetterModel.SHARES;

public class PostsAdapter extends BaseAdapter<AppCompatActivity, PlainPost> {
    private int lastPosition = -1;

    private AppCompatActivity activity;

    public PostsAdapter(List<PlainPost> posts, AppCompatActivity activity) {
        super(posts, activity);
        this.activity = activity;
    }

    protected PlainPost getPost(int position) {
        return items.get(position);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PlainPost post = getPost(position);
        if (holder instanceof ViewHolderPost) {
            ViewHolderPost postHolder = (ViewHolderPost) holder;

            postHolder.setLikesOnClick(v -> {
                DialogFragment fragment = PeopleListFragment.newInstance(Utils.getInstances(post), LIKES);
                fragment.show(activity.getSupportFragmentManager(), fragment.getTag());
            });

            postHolder.setRepostsOnClick(v -> {
                DialogFragment fragment = PeopleListFragment.newInstance(Utils.getInstances(post), SHARES);
                fragment.show(activity.getSupportFragmentManager(), fragment.getTag());
            });

            postHolder.hideDeleteButton();
        }

        holder.refresh(post);
        setItemsAppearingAnimation(holder.itemView, position);
    }

    public void notifyItemDeletedAtPosition(int pos) {
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, items.size());
    }

    private void setItemsAppearingAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(viewToAnimate, "translationY", Utils.getDefaultScreenHeight(), 0);
            animator.setDuration(400);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
            lastPosition = position;
        }
    }
}
