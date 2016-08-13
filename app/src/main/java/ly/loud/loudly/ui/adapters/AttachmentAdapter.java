package ly.loud.loudly.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.plain.PlainImage;
import solid.collections.SolidList;

import static ly.loud.loudly.util.ListUtils.asSolidList;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentHolder>{

    @NonNull
    private ArrayList<Attachment> attachmentList;

    public AttachmentAdapter(@NonNull SolidList<Attachment> attachmentList) {
        this.attachmentList = new ArrayList<>(attachmentList);
    }

    @Override
    public AttachmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AttachmentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentHolder holder, int position) {
        holder.refresh(attachmentList.get(position));
    }

    @Override
    public int getItemCount() {
        return attachmentList.size();
    }

    @NonNull
    public SolidList<Attachment> getAttachmentList() {
        return asSolidList(attachmentList);
    }

    public void setAttachmentList(@NonNull SolidList<Attachment> attachmentList) {
        this.attachmentList = new ArrayList<>(attachmentList);
        notifyDataSetChanged();
    }

    public void addAttachment(@NonNull Attachment attachment) {
        attachmentList.add(attachment);
        notifyItemInserted(attachmentList.size() - 1);
    }

    public int getAttachmentsCount() {
        return attachmentList.size();
    }

    protected class AttachmentHolder extends RecyclerView.ViewHolder {

        @SuppressWarnings("NullableProblems")
        @BindView(R.id.image)
        @NonNull
        ImageView imageView;

        public AttachmentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void refresh(@NonNull Attachment attachment) {
            if (attachment instanceof PlainImage) {
                PlainImage image = (PlainImage) attachment;
                Glide.with(itemView.getContext()).load(image.getUrl()).centerCrop().into(imageView);
            } else {
                Log.e("ATTACHMENT_HOLDER", "Attachment is not an instance of " + PlainImage.class.getName());
            }
        }
    }
}
