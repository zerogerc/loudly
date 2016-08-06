package ly.loud.loudly.ui.brand_new.full_post;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.new_base.Comment;
import ly.loud.loudly.new_base.Networks.Network;
import solid.collections.SolidList;

public interface FullPostInfoView extends MvpView {
    void onNewCommentsFromNetwork(@NonNull SolidList<Comment> comments, @Network int network);

    void onError(@StringRes int errorRes);
}
