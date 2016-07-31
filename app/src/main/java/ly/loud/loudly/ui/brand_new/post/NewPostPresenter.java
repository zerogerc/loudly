package ly.loud.loudly.ui.brand_new.post;

import android.support.annotation.NonNull;

import java.util.List;

import ly.loud.loudly.application.models.PostUploadModel;
import ly.loud.loudly.ui.brand_new.BasePresenter;

public class NewPostPresenter extends BasePresenter<NewPostView> {

    @NonNull
    PostUploadModel postUploadModel;

    public NewPostPresenter(@NonNull PostUploadModel postUploadModel) {
        this.postUploadModel = postUploadModel;
    }

    public void loadImage() {
    }

    public void uploadPost(List<Integer> networks) {
    }
}
