package ly.loud.loudly.util.database;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Component for database
 *
 * @author Danil Kolikov
 */
@Singleton
@Component(modules = {PostDbModule.class, KeysDbModule.class})
public interface DatabaseComponent {
    /**
     * Get database that stores Post, Keys, Links and Location tables
     *
     * @return Post Database
     */
    @Named("post") StorIOSQLite getPostsDatabase();

    /**
     * Get database that stores keys
     *
     * @return Keys database
     */
    @Named("key") StorIOSQLite getKeysDatabase();
}
