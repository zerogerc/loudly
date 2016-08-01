package ly.loud.loudly.ui.brand_new.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;

public class TextPlusAttachmentsView extends LinearLayout {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.text)
    @NonNull
    EditText textView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.attachments)
    @NonNull
    RecyclerView attachmentsView;

    @Nullable
    private OnAttachmentListener onAttachmentListener;

    private List<Attachment> attachmentList = new ArrayList<>();

    @NonNull
    private AttachmentAdapter adapter;

    public TextPlusAttachmentsView(Context context) {
        super(context);
    }

    public TextPlusAttachmentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextPlusAttachmentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextPlusAttachmentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.bind(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        attachmentsView.setLayoutManager(layoutManager);

        adapter = new AttachmentAdapter(attachmentList);
        attachmentsView.setAdapter(adapter);
    }

    public void setOnAttachmentListener(@Nullable OnAttachmentListener onAttachmentListener) {
        this.onAttachmentListener = onAttachmentListener;
    }

    @Nullable
    public OnAttachmentListener getOnAttachmentListener() {
        return onAttachmentListener;
    }

    @NonNull
    public String getText() {
        return textView.getText().toString();
    }

    public void addAttachment(@NonNull Attachment attachment) {
        attachmentList.add(attachment);
        setEditTextParams();
        setAttachmentsParams();
        adapter.notifyDataSetChanged();

        if (onAttachmentListener != null) {
            onAttachmentListener.onAttachmentAdded(attachment);
        }
    }

    private void setEditTextParams() {
        ViewGroup.LayoutParams params = textView.getLayoutParams();
        if (attachmentList.isEmpty()) {
            params.height = LayoutParams.MATCH_PARENT;
        } else {
            params.height = LayoutParams.WRAP_CONTENT;
        }
        textView.setLayoutParams(params);
    }

    private void setAttachmentsParams() {
        ViewGroup.LayoutParams params = attachmentsView.getLayoutParams();
        if (attachmentList.isEmpty()) {
            params.height = 0;
        } else {
            params.height = getContext().getResources().getDimensionPixelSize(R.dimen.text_with_attachment_attachment_size);
        }
        attachmentsView.setLayoutParams(params);
    }

    public interface OnAttachmentListener {
        void onAttachmentAdded(@NonNull Attachment attachment);

        void onAttachmentRemoved(@NonNull Attachment attachment);
    }
}
