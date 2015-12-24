package ly.loud.loudly.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import base.Person;
import base.Tasks;
import base.says.Comment;
import ly.loud.loudly.PeopleListFragment;
import ly.loud.loudly.R;
import util.Utils;

/**
 * Created by ZeRoGerc on 25.12.15.
 */
public class ViewHolderComment extends ViewHolder {
    ImageView icon;
    TextView name;
    TextView text;
    ImageView likesButton;
    TextView likesAmount;
    TextView commentTime;

    public ViewHolderComment(Activity activity, ViewGroup parent) {
        super(activity, LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_comment, parent, false));

        Item item = new Comment();

        icon = ((ImageView) itemView.findViewById(R.id.comment_avatar));
        name = ((TextView) itemView.findViewById(R.id.comment_name));
        text = ((TextView) itemView.findViewById(R.id.comment_text));
        likesAmount = ((TextView) itemView.findViewById(R.id.comment_likes_amount));
        likesButton = ((ImageView) itemView.findViewById(R.id.comment_likes_button));
        commentTime = ((TextView) itemView.findViewById(R.id.comment_time));
        refresh(item);
    }

    @Override
    public void refresh(Item item) {
        final Comment comment = ((Comment) item);
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

        likesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PeopleListFragment.showPersons(getActivity(), comment, Tasks.LIKES);
            }
        });
    }
}
