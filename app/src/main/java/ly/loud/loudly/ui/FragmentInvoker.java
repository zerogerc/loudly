package ly.loud.loudly.ui;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public interface FragmentInvoker {
    void startFragment(@NonNull Fragment fragment);
}
