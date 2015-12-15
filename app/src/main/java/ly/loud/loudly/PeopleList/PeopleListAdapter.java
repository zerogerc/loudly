package ly.loud.loudly.PeopleList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.List;

import base.Person;
import base.Tasks;
import base.says.Comment;
import ly.loud.loudly.Loudly;
import ly.loud.loudly.R;
import util.Utils;

public class PeopleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Item> items;
    private Activity activity;
    private PeopleListFragment fragment;

    private void loadAvatar(final Person person, final ImageView icon) {
        if (person.getPhotoUrl() != null) {
            Glide.with(Loudly.getContext())
                    .load(person.getPhotoUrl())
                    .asBitmap()
                    .override(Utils.dpToPx(48), Utils.dpToPx(48))
                    .fitCenter()
                    .into(new BitmapImageViewTarget(icon) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(Loudly.getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            icon.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            Glide.with(Loudly.getContext())
                    .load(R.mipmap.ic_launcher)
                    .override(Utils.dpToPx(48), Utils.dpToPx(48))
                    .fitCenter()
                    .into(icon);
        }
    }

    private void loadName(Person person, TextView name) {
        if (person.getFirstName() != null && person.getLastName() != null) {
            String text = person.getFirstName() + " " + person.getLastName();
            name.setText(text);
        } else {
            name.setText("");
        }
    }

    void refreshFields(final RecyclerView.ViewHolder viewHolder, final Item item) {
        if (item instanceof Person) {
            ViewHolderPerson holder = (ViewHolderPerson) viewHolder;
            Person person = ((Person) item);

            loadAvatar(person, holder.icon);
            loadName(person, holder.name);
            return;
        }

        if (item instanceof Comment) {
            ViewHolderComment holder = ((ViewHolderComment) viewHolder);
            Comment comment = ((Comment) item);
            Person person = comment.getPerson();

            loadAvatar(person, holder.icon);
            loadName(person, holder.name);

            if (comment.getText() != null) {
                holder.text.setText(comment.getText());
            } else {
                holder.text.setText("");
            }

            if (comment.getInfo() != null) {
                if (comment.getInfo().like == 0) {
                    holder.likesButton.setVisibility(View.GONE);
                    holder.likesAmount.setVisibility(View.GONE);
                } else {
                    holder.likesButton.setVisibility(View.VISIBLE);
                    holder.likesAmount.setVisibility(View.VISIBLE);
                    holder.likesAmount.setText(Integer.toString(comment.getInfo().like));
                }

                holder.commentTime.setVisibility(View.VISIBLE);
                holder.commentTime.setText(Utils.getDateFormatted(comment.getDate()));
            } else {
                holder.likesAmount.setVisibility(View.GONE);
                holder.commentTime.setVisibility(View.GONE);
            }

            holder.likesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PeopleListFragment.showPersons(activity, (Comment) item, Tasks.LIKES);
                }
            });


            return;
        }

        ViewHolderDelimiter holder = (ViewHolderDelimiter) viewHolder;
        NetworkDelimiter delimiter = ((NetworkDelimiter) item);
        Glide.with(Loudly.getContext())
                .load(Utils.getResourceByNetwork(delimiter.getNetwork()))
                .override(Utils.dpToPx(48), Utils.dpToPx(48))
                .fitCenter()
                .into(holder.icon);

    }

    public PeopleListAdapter(List<Item> items, Activity activity, PeopleListFragment fragment) {
        this.items = items;
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Person) {
            return Item.PERSON;
        } else if (items.get(position) instanceof Comment){
            return Item.COMMENT;
        } else {
            return Item.DELIMITER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Item.PERSON:
                View personView = LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_person, parent, false);
                return new ViewHolderPerson(personView, new Person());
            case Item.COMMENT:
                View commentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_comment, parent, false);
                return new ViewHolderComment(commentView, new Comment());
            default:  //Item.DELIMITER
                View delimiterView = LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_delimeter, parent, false);
                return new ViewHolderDelimiter(delimiterView, new NetworkDelimiter());
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) fragment.hideProgress();
        refreshFields(holder, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolderDelimiter extends RecyclerView.ViewHolder {
        ImageView icon;

        public ViewHolderDelimiter(View itemView, Item item) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.people_list_delimeter_icon);
            refreshFields(this, item);
        }

    }

    class ViewHolderPerson extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolderPerson(View itemView, Item item) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.people_list_person_avatar);
            name = (TextView) itemView.findViewById(R.id.people_list_person_name);
            refreshFields(this, item);
        }
    }

    class ViewHolderComment extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView text;
        ImageView likesButton;
        TextView likesAmount;
        TextView commentTime;

        public ViewHolderComment(View itemView, Item item) {
            super(itemView);

            icon = ((ImageView) itemView.findViewById(R.id.comment_avatar));
            name = ((TextView) itemView.findViewById(R.id.comment_name));
            text = ((TextView) itemView.findViewById(R.id.comment_text));
            likesAmount = ((TextView) itemView.findViewById(R.id.comment_likes_amount));
            likesButton = ((ImageView) itemView.findViewById(R.id.comment_likes_button));
            commentTime = ((TextView) itemView.findViewById(R.id.comment_time));
            refreshFields(this, item);
        }
    }
}
