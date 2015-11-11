package util;

import android.app.Activity;

public interface ResponseListener {
    void onSuccess(Activity activity, Object result);
    void onFail(Activity activity, String error);
}
