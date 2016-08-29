package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import dagger.Module;
import dagger.Provides;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.util.database.entities.*;
import ly.loud.loudly.util.database.entities.links.Links;
import ly.loud.loudly.util.database.entities.links.LinksTypeMapping;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Module for Post Database
 *
 * @author Danil Kolikov
 */

@Module
public class PostDbModule {
    @NonNull
    Loudly loudlyApplication;

    public PostDbModule(@NonNull Loudly loudlyApplication) {
        this.loudlyApplication = loudlyApplication;
    }

    /**
     * Provide Post Database
     * @param postDbHelper Post Helper
     * @return Post Database
     */
    @Provides
    @Named("post")
    @NonNull
    @Singleton
    public StorIOSQLite provideStorIOSQLite(@NonNull PostDbHelper postDbHelper) {
        return DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(postDbHelper)
                .addTypeMapping(StoredLocation.class, new StoredLocationSQLiteTypeMapping())
                .addTypeMapping(StoredAttachment.class, new StoredAttachmentSQLiteTypeMapping())
                .addTypeMapping(Links.class, new LinksTypeMapping())
                .addTypeMapping(StoredPost.class, new StoredPostSQLiteTypeMapping())
                .addTypeMapping(StoredEvent.class, new StoredEventSQLiteTypeMapping())
                .build();
    }

    /**
     * Provide Helper for Post Database
     * @return The Helper
     */
    @Provides
    @NonNull
    @Singleton
    public PostDbHelper providePostDbHelper() {
        return new PostDbHelper(loudlyApplication);
    }
}
