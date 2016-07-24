package ly.loud.loudly.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CommentsGetterModel;
import ly.loud.loudly.application.models.PeopleGetterModel;
import ly.loud.loudly.base.Networks;
import ly.loud.loudly.base.Person;
import ly.loud.loudly.base.attachments.Attachment;
import ly.loud.loudly.base.attachments.Image;
import ly.loud.loudly.base.says.Comment;
import ly.loud.loudly.base.says.LoudlyPost;
import ly.loud.loudly.base.says.Post;
import ly.loud.loudly.base.says.Say;
import ly.loud.loudly.ui.adapter.Item;
import ly.loud.loudly.ui.adapter.NetworkDelimiter;
import ly.loud.loudly.ui.views.GlideImageView;
import ly.loud.loudly.util.Utils;
import rx.schedulers.Schedulers;

import static ly.loud.loudly.application.models.PeopleGetterModel.LIKES;
import static ly.loud.loudly.application.models.PeopleGetterModel.SHARES;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class FullPostInfoActivity extends AppCompatActivity {

    public static final String POST_KEY = "post";

    @BindView(R.id.full_post_info_post_footer)
    View postFooter;

    @BindView(R.id.full_post_info_content_list)
    LinearLayout content;

    @BindView(R.id.full_post_info_likers_avatars)
    LinearLayout likersContent;

    @BindView(R.id.full_post_info_network_icon)
    ImageView postIcon;

    @BindView(R.id.full_post_info_time)
    TextView timeView;

    @BindView(R.id.full_post_info_post_text)
    TextView postText;

    @BindView(R.id.full_post_info_post_image)
    GlideImageView postImage;

    @BindView(R.id.full_post_info_shares_amount)
    TextView sharesAmount;

    @BindView(R.id.full_post_info_shares_button)
    ImageView sharesButton;

    @BindView(R.id.full_post_info_likes_amount)
    TextView likesAmount;

    @BindView(R.id.full_post_info_likes_button)
    ImageView likesButton;

    @BindView(R.id.activity_full_post_progress)
    ProgressBar progressBar;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    PeopleGetterModel peopleGetterModel;

    @SuppressWarnings("NullableProblems") // Inject
    @Inject
    @NonNull
    CommentsGetterModel commentsGetterModel;

    private ArrayList<Item> comments;

    private ArrayList<Item> likers;

    private Post post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post_info);

        ButterKnife.bind(this);
        Loudly.getContext().getAppComponent().inject(this);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);

        handleIntent(getIntent());
    }

    /**
     * Handling of given intent
     * @param intent given intent
     */
    private void handleIntent(Intent intent) {
        Post prev = post;
        post = intent.getParcelableExtra(POST_KEY);
        if (prev == null || Say.COMPARATOR.compare(prev, post) != 0) {
            loadPostView();

            commentsGetterModel.getComments(post)
                    .subscribeOn(Schedulers.io())
                    .observeOn(mainThread())
                    .subscribe(commentsFromNetworks -> {
                        comments = new ArrayList<>();
                        for (CommentsGetterModel.CommentsFromNetwork networkComments : commentsFromNetworks) {
                            if (!networkComments.comments.isEmpty()) {
                                comments.add(new NetworkDelimiter(networkComments.network));
                                comments.addAll(networkComments.comments);
                            }
                        }

                        inflateFooter();
                        inflateComments();
                    });

            peopleGetterModel.getPersonsByType(post, LIKES)
                    .subscribeOn(Schedulers.io())
                    .observeOn(mainThread())
                    .doOnError(throwable -> {
                        // TODO: say user that something goes wrong
                    })
                    .subscribe(personsFromNetworks -> {
                        likers = new ArrayList<>();
                        for (PeopleGetterModel.PersonsFromNetwork list : personsFromNetworks) {
                            likers.addAll(list.persons);
                        }

                        fillLikers();
                    });
        }
    }

    private void fillLikers() {
        findViewById(R.id.full_post_info_post_footer).setVisibility(View.VISIBLE);

        int added = 0;
        for (Item item : likers) {
            if (item instanceof Person) {
                final GlideImageView avatar = ((GlideImageView) LayoutInflater.from(this).inflate(R.layout.full_post_info_feedback_avatar, likersContent, false));
                likersContent.addView(avatar);
                //set width so it not be equal to 0
                //avatar.getLayoutParams().width = ((int) getResources().getDimension(R.dimen.feedback_icon_size));
                avatar.loadCircularShapeImageByUrl(((Person) item).getPhotoUrl());
                added++;
            }

            //TODO: do not hardcode
            if (added > 4) {
                break;
            }
        }
    }

    private void setListeners() {
        if (post.getInfo().like > 0) {
            View.OnClickListener likeListener = v -> {
                DialogFragment fragment = PeopleListFragment.newInstance(post, LIKES);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            };
            findViewById(R.id.full_post_info_likes_button).setOnClickListener(likeListener);
            findViewById(R.id.full_post_info_likers_avatars).setOnClickListener(likeListener);
        }

        if (post.getInfo().repost > 0) {
            findViewById(R.id.full_post_info_shares_button)
                    .setOnClickListener(v -> {
                        DialogFragment fragment = PeopleListFragment.newInstance(post, SHARES);
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
                    });
        }
    }

    /**
     * Inflate footer of post based of {@link #likers}
     */
    private void inflateFooter() {
        postFooter.setVisibility(View.VISIBLE);

        int gray_color = ContextCompat.getColor(this, R.color.light_grey_color);

        if (post.getInfo().repost > 0) {
            sharesAmount.setText(Integer.toString(post.getInfo().repost));
        } else {
            sharesButton.setColorFilter(gray_color);
            sharesButton.setVisibility(View.INVISIBLE);
        }

        if (post.getInfo().like > 0) {
            likesAmount.setText(Integer.toString(post.getInfo().like));
        } else {
            likesButton.setColorFilter(gray_color);
            likesButton.setVisibility(View.INVISIBLE);
        }

        setListeners();
    }

    /**
     * Inflate content with comments from {@link #comments}
     */
    private void inflateComments() {
        progressBar.setVisibility(View.GONE);

        for (Item item : comments) {
            if (item instanceof Comment) {
                View comment = LayoutInflater.from(this).inflate(R.layout.full_post_info_comment, content, false);
                content.addView(comment);
                loadComment(comment, ((Comment) item));
            } else if ((post instanceof LoudlyPost) && (item instanceof NetworkDelimiter)) {
                content.addView(LayoutInflater.from(this).inflate(R.layout.full_post_info_comment_delimiter, content, false));

                View delimiter = LayoutInflater.from(this).inflate(R.layout.full_post_info_network_delimiter, content, false);
                content.addView(delimiter);

                ImageView icon = ((ImageView) delimiter.findViewById(R.id.full_post_info_delimiter_icon));
                icon.setImageResource(Utils.getResourceByNetwork(((NetworkDelimiter) item).getNetwork()));

                TextView name = ((TextView) delimiter.findViewById(R.id.full_post_info_delimiter_name));
                name.setText(Networks.nameOfNetwork(((NetworkDelimiter) item).getNetwork()));
            }
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
            commentView.findViewById(R.id.comment_likes_button)
                    .setOnClickListener(v -> {
                        DialogFragment fragment = PeopleListFragment.newInstance(comment, LIKES);
                        fragment.show(getSupportFragmentManager(), fragment.getTag());
                    });
        } else {
            commentView.findViewById(R.id.comment_likes_button).setVisibility(View.GONE);
            commentView.findViewById(R.id.comment_likes_amount).setVisibility(View.GONE);
        }
    }

    /**
     * Set the UI comments of Post using given {@link #post}
     */
    private void loadPostView() {
        if (post == null) {
            return;
        }

        postIcon.setImageResource(Utils.getResourceByNetwork(post.getNetwork()));
        timeView.setText(Utils.getDateFormatted(post.getDate()));
        postText.setText(post.getText());

        for (Attachment attachment : post.getAttachments()) {
            if (attachment instanceof Image) {
                postImage.loadImage(((Image) attachment));
            }
        }

        //Set footer visibility to GONE while we don't receive persons who like and share
        postFooter.setVisibility(View.GONE);
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
}
