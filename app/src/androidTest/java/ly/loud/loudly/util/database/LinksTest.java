package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.Link;
import ly.loud.loudly.util.Equality;
import ly.loud.loudly.test.Generators;

/**
 * @author Danil Kolikov
 */
public class LinksTest extends DatabaseTest<Link[]> {
    @NonNull
    @Override
    protected Link[] generate() {
        return Generators.randomLinks(20, random);
    }

    @NonNull
    @Override
    protected Link[] get(long id) throws DatabaseException {
        return DatabaseUtils.loadLinks(id);
    }

    @Override
    protected void delete(long id) throws DatabaseException {
        DatabaseUtils.deleteLinks(id);
    }

    @Override
    protected long insert(@NonNull Link[] object) throws DatabaseException {
        return DatabaseUtils.saveLinks(object);
    }

    @Override
    protected boolean equals(@Nullable Link[] a, @Nullable Link[] b) {
        return Equality.equal(a, b);
    }
}
