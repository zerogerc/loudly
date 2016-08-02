package ly.loud.loudly.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ly.loud.loudly.new_base.Person;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.R;
import ly.loud.loudly.util.Utils;

/**
 * Created by ZeRoGerc on 25.12.15.
 * ITMO University
 */

public class ViewHolderComment extends ViewHolder<Comment> {
    private ImageView icon;
    private TextView name;
    private TextView text;
    private ImageView likesButton;
    private TextView likesAmount;
    private TextView commentTime;

    public ViewHolderComment(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.full_post_info_comment, parent, false));

        Comment comment = new Comment();

        icon = ((ImageView) itemView.findViewById(R.id.comment_avatar));
        name = ((TextView) itemView.findViewById(R.id.comment_name));
        text = ((TextView) itemView.findViewById(R.id.comment_text));
        likesAmount = ((TextView) itemView.findViewById(R.id.comment_likes_amount));
        likesButton = ((ImageView) itemView.findViewById(R.id.comment_likes_button));
        commentTime = ((TextView) itemView.findViewById(R.id.comment_time));
        refresh(comment);
    }

    @Override
    public void refresh(final Comment comment) {
        final Person person = comment.getPerson();

        Utils.loadAvatar(person, icon);
        Utils.loadName(person, name);

        if (comment.getText() != null) {
            text.setText(comment.getText());
        } else {
            text.setText("");
        }

        if (comment.getInfo() != null) {
            if (comment.getInfo().like == 0) {
                likesButton.setVisibility(View.GONE);
                likesAmount.setVisibility(View.GONE);
            } else {
                likesButton.setVisibility(View.VISIBLE);
                likesAmount.setVisibility(View.VISIBLE);
                likesAmount.setText(Integer.toString(comment.getInfo().like));
            }

            commentTime.setVisibility(View.VISIBLE);
            commentTime.setText(Utils.getDateFormatted(comment.getDate()));
        } else {
            likesAmount.setVisibility(View.GONE);
            commentTime.setVisibility(View.GONE);
        }
    }

    public void setLikesOnClick(View.OnClickListener listener) {
        likesButton.setOnClickListener(listener);
    }
}
