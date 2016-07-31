package ly.loud.loudly.ui.brand_new.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.base.attachments.Attachment;

public class TextPlusAttachmentsView extends LinearLayout {

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.text)
    @NonNull
    EditText textView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.attachments)
    @NonNull
    RecyclerView attachementsView;

    @Nullable
    private OnAttachmentListener onAttachmentListener;

    private List<Attachment> attachmentList = new ArrayList<>();

    public TextPlusAttachmentsView(Context context) {
        super(context);
        init();
    }

    public TextPlusAttachmentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextPlusAttachmentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextPlusAttachmentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
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

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);
    }

    public interface OnAttachmentListener {
        void onAttachmentAdded(@NonNull Attachment attachment);

        void onAttachmentRemoved(@NonNull Attachment attachment);
    }
}
