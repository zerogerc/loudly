package ly.loud.loudly.ui.brand_new.post;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import ly.loud.loudly.R;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public class NetworkChooseFragment extends BottomSheetDialogFragment {

    public static final String RESIZING_DIALOG = "resizing_dialog";

    @Nullable
    DialogFragment dialogFragment;

    @Nullable
    private Button fixedButton;

    private final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (dialogFragment != null) {
                dialogFragment.getView().setPadding(0, 0, 0, ((int) (slideOffset * 10)));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            dialogFragment = ((DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(RESIZING_DIALOG));
        } catch (ClassCastException e) {
            dialogFragment = null;
        }
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.network_choose_layout, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams());
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallback);
            ((BottomSheetBehavior) behavior).setState(STATE_EXPANDED);
        }
    }
}
