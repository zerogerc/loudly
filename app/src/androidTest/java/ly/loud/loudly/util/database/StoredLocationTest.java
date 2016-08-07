package ly.loud.loudly.util.database;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import ly.loud.loudly.util.Equality;
import ly.loud.loudly.test.Generators;
import ly.loud.loudly.util.database.entities.StoredLocation;

/**
 * Test of Locations Database
 *
 * @author Danil Kolikov
 */
public class StoredLocationTest extends DatabaseTest<StoredLocation> {
    @CheckResult
    @NonNull
    @Override
    protected StoredLocation generate() {
        return Generators.randomStoredLocation(20, random);
    }

    @CheckResult
    @NonNull
    @Override
    protected StoredLocation get(@NonNull StoredLocation object) throws DatabaseException {
        Assert.assertNotNull(object.getId());
        return DatabaseUtils.loadStoredLocation(object.getId());
    }

    @Override
    protected void delete(@NonNull StoredLocation object) throws DatabaseException {
        Long id = object.getId();
        Assert.assertNotNull(id);
        DatabaseUtils.deleteLocation(id);
    }

    @CheckResult
    @NonNull
    @Override
    protected StoredLocation insert(@NonNull StoredLocation object) throws DatabaseException {
        return DatabaseUtils.saveStoredLocation(object);
    }

    @Override
    protected boolean equals(@Nullable StoredLocation a, @Nullable StoredLocation b) {
        return Equality.equal(a, b);
    }
}
