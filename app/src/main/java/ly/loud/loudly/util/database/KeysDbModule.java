package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.util.database.entities.Key;
import ly.loud.loudly.util.database.entities.KeySQLiteTypeMapping;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Module for Keys database
 *
 * @author Danil Kolikov
 */
@Module
public class KeysDbModule {
    @NonNull
    Loudly loudlyApplication;

    public KeysDbModule(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    /**
     * Provide Keys Database
     * @param keysDbHelper Keys Helper
     * @return Keys Database
     */
    @Provides
    @Named("key")
    @NonNull
    @Singleton
    public StorIOSQLite provideStorIOSQLite(@NonNull KeysDbHelper keysDbHelper) {
        return DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(keysDbHelper)
                .addTypeMapping(Key.class, new KeySQLiteTypeMapping())
                .build();
    }

    /**
     * Provide helper for Keys Database
     * @return The Helper
     */
    @Provides
    @NonNull
    @Singleton
    public KeysDbHelper provideKeysDbHelper() {
        return new KeysDbHelper(loudlyApplication);
    }
}
