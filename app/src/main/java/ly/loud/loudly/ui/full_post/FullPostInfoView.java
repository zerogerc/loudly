package ly.loud.loudly.ui.full_post;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.hannesdorfmann.mosby.mvp.MvpView;

import ly.loud.loudly.base.single.Comment;
import ly.loud.loudly.networks.Networks.Network;
import solid.collections.SolidList;

public interface FullPostInfoView extends MvpView {
    void onNewCommentsFromNetwork(@NonNull SolidList<Comment> comments, @Network int network);

    void onError(@StringRes int errorRes);
}
