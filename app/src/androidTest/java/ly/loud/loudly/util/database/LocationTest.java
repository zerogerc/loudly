package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.Location;
import ly.loud.loudly.util.Equality;
import ly.loud.loudly.test.Generators;

/**
 * Test of Locations Database
 *
 * @author Danil Kolikov
 */
public class LocationTest extends DatabaseTest<Location> {
    @NonNull
    @Override
    protected Location generate() {
        return Generators.randomLocation(20, random);
    }

    @NonNull
    @Override
    protected Location get(long id) throws DatabaseException {
        return DatabaseUtils.loadLocation(id);
    }

    @Override
    protected void delete(long id) throws DatabaseException {
        DatabaseUtils.deleteLocation(id);
    }

    @Override
    protected long insert(@NonNull Location object) throws DatabaseException {
        return DatabaseUtils.saveLocation(object);
    }

    @Override
    protected boolean equals(@Nullable Location a, @Nullable Location b) {
        return Equality.equal(a, b);
    }
}
