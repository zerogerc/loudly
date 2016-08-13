package ly.loud.loudly.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ly.loud.loudly.R;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.ui.adapters.AttachmentAdapter;
import ly.loud.loudly.util.ListUtils;
import solid.collections.SolidList;

public class TextPlusAttachmentsView extends LinearLayout {

    private static final String SUPER_STATE = "super_state";
    private static final String ATTACHMENTS = "attachments";

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.text)
    @NonNull
    EditText textView;

    @SuppressWarnings("NullableProblems") // Butterknife
    @BindView(R.id.attachments)
    @NonNull
    RecyclerView attachmentsView;

    private int attachmentsCount = 0;

    @SuppressWarnings("NullableProblems") // onAttachedToWindow
    @NonNull
    private AttachmentAdapter adapter;

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
        adapter = new AttachmentAdapter(SolidList.empty());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.bind(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        attachmentsView.setLayoutManager(layoutManager);
        attachmentsView.setAdapter(adapter);
        if (adapter.getItemCount() > 0) {
            attachmentsView.setVisibility(VISIBLE);
        } else {
            attachmentsView.setVisibility(GONE);
        }
    }

    public void addAttachment(@NonNull Attachment attachment) {
        if (adapter.getAttachmentsCount() == 0) {
            attachmentsView.setVisibility(VISIBLE);
        }
        adapter.addAttachment(attachment);
    }

    @NonNull
    public String getText() {
        return textView.getText().toString();
    }

    @NonNull
    public List<Attachment> getAttachmentList() {
        return adapter.getAttachmentList();

    }

    @Override
    @NonNull
    public Parcelable onSaveInstanceState () {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        state.putParcelableArrayList(ATTACHMENTS, new ArrayList<>(adapter.getAttachmentList()));
        return state;
    }

    @Override
    public void onRestoreInstanceState (@NonNull Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle)state;
            //noinspection WrongConstant
            ArrayList<Attachment> attachments = savedState.getParcelableArrayList(ATTACHMENTS);
            if (attachments == null) {
                adapter.setAttachmentList(SolidList.empty());
            } else {
                adapter.setAttachmentList(ListUtils.asSolidList(attachments));
            }
            Parcelable superState = savedState.getParcelable(SUPER_STATE);
            super.onRestoreInstanceState(superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
