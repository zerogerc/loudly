package ly.loud.loudly.ui.adapter.person;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;

import ly.loud.loudly.R;

public class ViewHolderPersonPresenter {

    private @NonNull Activity activity;

    public ViewHolderPersonPresenter(@NonNull Activity activity) {
        this.activity = activity;
    }

    public void openWebView(final @Nullable String url) {
        if (url != null) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

            int colorPrimary;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                colorPrimary = activity.getResources().getColor(
                        R.color.colorPrimary,
                        activity.getTheme()
                );
            } else {
                colorPrimary = activity.getResources().getColor(R.color.colorPrimary);
            }
            builder.setToolbarColor(colorPrimary);

            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(activity, Uri.parse(url));
        }
    }
}
