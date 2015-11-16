package util;

import android.content.Context;

public interface ResultListener {
    void onSuccess(Context context, Object result);
    void onFail(Context context, String error);
}
