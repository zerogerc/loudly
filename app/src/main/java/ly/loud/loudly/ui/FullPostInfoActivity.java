package ly.loud.loudly.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ly.loud.loudly.R;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.Tasks;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.base.says.Say;
import ly.loud.loudly.ui.adapter.Item;
import ly.loud.loudly.ui.adapter.NetworkDelimiter;
import ly.loud.loudly.ui.views.GlideImageView;
import ly.loud.loudly.util.AttachableReceiver;
import ly.loud.loudly.util.Broadcasts;
import ly.loud.loudly.util.Utils;

public class FullPostInfoActivity extends AppCompatActivity {
    public static final String POST_KEY = "post";

    private ArrayList<Item> elements;
    private ArrayList<Item> likers;
    private Post post;

    private LinearLayout content;
    private LinearLayout likersContent;
    private static CommentsReceiver receiver;

    private int broadcastReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post_info);

        getSupportActionBar().setHomeButtonEnabled(true);

        elements = new ArrayList<>();
        likers = new ArrayList<>();

        content = ((LinearLayout) findViewById(R.id.full_post_info_content_list));
        likersContent = ((LinearLayout) findViewById(R.id.full_post_info_likers_avatars));

        receiver = new CommentsReceiver(this);

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiver != null) {
            receiver.attach(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (receiver != null) {
            receiver.detach();
        }

    }

    /**
     * Handling of given intent
     * @param intent given intent
     */
    private void handleIntent(Intent intent) {
        Post prev = post;
        post = intent.getParcelableExtra(POST_KEY);
        if (prev == null || Say.COMPARATOR.compare(prev, post) != 0) {
            //set elements to new array list in order not to receive comments from previous query
            //previous query just load comments to null list
            elements = new ArrayList<>();
            likers = new ArrayList<>();

            loadPostView();

            broadcastReceived = 0;

            Tasks.CommentsGetter taskComments = new Tasks.CommentsGetter(post, elements, Loudly.getContext().getWraps());
            taskComments.execute();

            //TODO: add peoples who share
            Tasks.PersonGetter taskPerson = new Tasks.PersonGetter(post, Tasks.LIKES, likers, Loudly.getContext().getWraps());
            taskPerson.execute();
        }
    }

    private void setListeners() {
        if (post.getInfo().like > 0) {
            View.OnClickListener likeListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(FullPostInfoActivity.this, post, Tasks.LIKES);
                }
            };
            findViewById(R.id.full_post_info_likes_button).setOnClickListener(likeListener);
            findViewById(R.id.full_post_info_likers_avatars).setOnClickListener(likeListener);
        }

        if (post.getInfo().repost > 0) {
            findViewById(R.id.full_post_info_shares_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(FullPostInfoActivity.this, post, Tasks.SHARES);
                }
            });
        }
    }

    /**
     * Inflate footer of post based of {@link #likers}
     */
    private void inflateFooter() {
        findViewById(R.id.full_post_info_post_footer).setVisibility(View.VISIBLE);

        int gray_color = ContextCompat.getColor(this, R.color.light_grey_color);

        if (post.getInfo().repost > 0) {
            TextView amount = ((TextView) findViewById(R.id.full_post_info_shares_amount));
            amount.setText(Integer.toString(post.getInfo().repost));
        } else {
            ImageView button = ((ImageView) findViewById(R.id.full_post_info_shares_button));
            button.setColorFilter(gray_color);
            findViewById(R.id.full_post_info_shares_amount).setVisibility(View.INVISIBLE);
        }

        if (post.getInfo().like > 0) {
            TextView amount = ((TextView) findViewById(R.id.full_post_info_likes_amount));
            amount.setText(Integer.toString(post.getInfo().like));
        } else {
            ImageView button = ((ImageView) findViewById(R.id.full_post_info_likes_button));
            button.setColorFilter(gray_color);
            findViewById(R.id.full_post_info_likes_amount).setVisibility(View.INVISIBLE);
        }

        setListeners();

        int added = 0;
        for (Item item : likers) {
            if (item instanceof Person) {
                final GlideImageView avatar = ((GlideImageView) LayoutInflater.from(this).inflate(R.layout.full_post_info_feedback_avatar, likersContent, false));
                likersContent.addView(avatar);
                //set width so it not be equal to 0
//                avatar.getLayoutParams().width = ((int) getResources().getDimension(R.dimen.feedback_icon_size));
                avatar.loadCircularShapeImageByUrl(((Person) item).getPhotoUrl());
                added++;
            }

            //TODO: do not hardcode
            if (added > 4) {
                break;
            }
        }
    }

    /**
     * Inflate content with comments from {@link #elements}
     */
    private void inflateComments() {
        //Remove all view except Post
        for (int i = 1; i < content.getChildCount(); i++) {
            content.removeViewAt(i);
        }

        for (Item item : elements) {
            if (item instanceof Comment) {
                View comment = LayoutInflater.from(this).inflate(R.layout.full_post_info_comment, null, false);
                content.addView(comment);
                loadComment(comment, ((Comment) item));
            } else if (item instanceof NetworkDelimiter) {
                View delimiter = LayoutInflater.from(this).inflate(R.layout.list_delimeter, null, false);
                content.addView(delimiter);
                ImageView icon = ((ImageView) delimiter.findViewById(R.id.people_list_delimeter_icon));
                icon.setImageResource(Utils.getResourceByNetwork(((NetworkDelimiter) item).getNetwork()));
            }
            content.addView(LayoutInflater.from(this).inflate(R.layout.full_post_info_comment_delimeter, null, false));
        }
    }

    /**
     * Load <code>comment</code> content to given <code>commentView</code>.
     * @param commentView given view
     * @param comment given View
     */
    private void loadComment(View commentView, final Comment comment) {
        GlideImageView avatar = ((GlideImageView) commentView.findViewById(R.id.comment_avatar));
        avatar.loadCircularShapeImageByUrl(comment.getPerson().getPhotoUrl());

        TextView name = ((TextView) commentView.findViewById(R.id.comment_name));
        name.setText(comment.getPerson().getFirstName() + " " + comment.getPerson().getLastName());

        TextView body = ((TextView) commentView.findViewById(R.id.comment_text));
        body.setText(comment.getText());

        TextView time = ((TextView) commentView.findViewById(R.id.comment_time));
        time.setText(Utils.getDateFormatted(comment.getDate()));

        if (comment.getInfo().like > 0) {
            commentView.findViewById(R.id.comment_likes_button).setVisibility(View.VISIBLE);
            TextView likes = ((TextView) commentView.findViewById(R.id.comment_likes_amount));
            likes.setText(Integer.toString(comment.getInfo().like));
            commentView.findViewById(R.id.comment_likes_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(FullPostInfoActivity.this, comment, Tasks.LIKES);
                }
            });
        } else {
            commentView.findViewById(R.id.comment_likes_button).setVisibility(View.GONE);
            commentView.findViewById(R.id.comment_likes_amount).setVisibility(View.GONE);
        }
    }

    /**
     * Set the UI elements of Post using given {@link #post}
     */
    private void loadPostView() {
        if (post == null) {
            return;
        }

        ImageView icon = ((ImageView) findViewById(R.id.full_post_info_network_icon));
        icon.setImageResource(Utils.getResourceByNetwork(post.getNetwork()));

        TextView time = ((TextView) findViewById(R.id.full_post_info_time));
        time.setText(Utils.getDateFormatted(post.getDate()));

        TextView postText = ((TextView) findViewById(R.id.full_post_info_post_text));
        postText.setText(post.getText());

        for (Attachment attachment : post.getAttachments()) {
            if (attachment instanceof Image) {
                GlideImageView image = ((GlideImageView) findViewById(R.id.full_post_info_post_image));
                image.loadImage(((Image) attachment));
            }
        }

        //Set footer visibility to GONE while we don't receive persons who like and share
        findViewById(R.id.full_post_info_post_footer).setVisibility(View.GONE);

        content.addView(new ProgressBar(this));
    }


    /**
     * As this Activity launched in <code>singleTask</code> mode we need to listen for intents,
     * and not create new instance of activity every time.
     * @param intent given intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * Receiver that listen for {@link Broadcasts#FINISHED}.
     * After {@link Broadcasts#FINISHED} received content would be inflated by comments.
     */
    private class CommentsReceiver extends AttachableReceiver<FullPostInfoActivity> {
        /**
         * Constructor from initial context and list of filters
         *
         * @param context initial context
         */
        public CommentsReceiver(FullPostInfoActivity context) {
            super(context, Broadcasts.GET_PERSONS);
        }

        @Override
        public void onMessageReceive(FullPostInfoActivity context, Intent message) {
            int status = message.getIntExtra(Broadcasts.STATUS_FIELD, -1);
            if (status == Broadcasts.FINISHED) {
                final String postId = message.getStringExtra(Broadcasts.ID_FIELD);
                if (post.getLink().get().equals(postId)) {
                    broadcastReceived++;
                }
                if (broadcastReceived == 2) {
                    inflateFooter();
                    inflateComments();
                    stop();
                }
            }
        }
    }
}
