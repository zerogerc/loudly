package ly.loud.loudly.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

import ly.loud.loudly.util.database.KeysDbModule;
import ly.loud.loudly.util.database.PostDbModule;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Core application of Loudly app
 * Stores run-time variables
 */
public class Loudly extends Application {
    private static final String TAG = "LOUDLY";

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private static Loudly context;

    @SuppressWarnings("NullableProblems") // Inject
    @NonNull
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        RxPaparazzo.register(this);
        appComponent = getComponent();

        appComponent
                .keysModel()
                .loadKeys()
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(result -> Log.i(TAG, "Keys loaded"),
                        error -> Log.e(TAG, "Can't load keys", error));
    }

    /**
     * Get context of the Application.
     * As the application can't die until user kills it, it's possible to store the context here
     *
     * @return link to the Loudly
     */
    @NonNull
    public static Loudly getContext() {
        return context;
    }

    @NonNull
    public static Loudly getApplication(@NonNull Context context) {
        return (Loudly) context.getApplicationContext();
    }

    @NonNull
    protected AppComponent getComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .keysDbModule(new KeysDbModule(this))
                .postDbModule(new PostDbModule(this))
                .build();
    }

    @NonNull
    public AppComponent getAppComponent() {
        return appComponent;
    }

}
