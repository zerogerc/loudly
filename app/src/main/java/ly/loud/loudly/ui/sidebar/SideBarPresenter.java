package ly.loud.loudly.ui.sidebar;

import android.support.annotation.NonNull;

import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.application.models.CoreModel;
import ly.loud.loudly.util.BasePresenter;

public class SideBarPresenter extends BasePresenter<SideBarView> {

    public SideBarPresenter(
            @NonNull Loudly loudlyApplication,
            @NonNull CoreModel coreModel
    ) {
        super(loudlyApplication);
    }


}
