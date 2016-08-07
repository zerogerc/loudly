package ly.loud.loudly.util.database;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ly.loud.loudly.test.Generators;
import ly.loud.loudly.util.database.entities.StoredAttachment;
import ly.loud.loudly.util.database.entities.StoredLocation;
import ly.loud.loudly.util.database.entities.StoredPost;
import ly.loud.loudly.util.database.entities.links.Links;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Danil Kolikov
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class DatabaseTest<T> {
    @NonNull
    protected Random random = new Random(1234567890L);

    @NonNull
    protected StorIOSQLite postsDatabase = DatabaseUtils.getPostsDatabase();

    public static void cleanTables(@NonNull StorIOSQLite database, @NonNull String... tables) {
        for (String table : tables) {
            database
                    .delete()
                    .byQuery(DeleteQuery
                            .builder()
                            .table(table)
                            .build())
                    .prepare()
                    .executeAsBlocking();
        }
    }

    @CheckResult
    @NonNull
    protected abstract T generate();

    @CheckResult
    @NonNull
    protected abstract T get(@NonNull T object) throws DatabaseException;

    protected abstract void delete(@NonNull T object) throws DatabaseException;

    protected abstract boolean equals(@Nullable T a, @Nullable T b);

    @CheckResult
    @NonNull
    protected abstract T insert(@NonNull T object) throws DatabaseException;

    private void checkStored(@NonNull T object) throws DatabaseException {
        T stored = get(object);
        assertTrue(equals(stored, object));
    }

    private void checkAbsent(T object) {
        try {
            T result = get(object);
            throw new AssertionError("Post wasn't deleted");
        } catch (DatabaseException e) {
            // That's fine
        }
    }

    @NonNull
    private ArrayList<T> generate(int size) {
        return Generators.generateArrayList(size, this::generate);
    }

    @CheckResult
    @NonNull
    private List<T> insert(@NonNull List<T> objects) throws DatabaseException {
        List<T> ids = new ArrayList<>();
        for (T object : objects) {
            ids.add(insert(object));
        }
        return ids;
    }

    private void delete(@NonNull List<T> ids) throws DatabaseException {
        for (int i = 0; i < ids.size(); i++) {
            delete(ids.get(i));
        }
    }

    private void checkStored(@NonNull List<T> ids) throws DatabaseException {
        for (int i = 0; i < ids.size(); i++) {
            checkStored(ids.get(i));
        }
    }

    private void checkAbsent(@NonNull List<T> objects) {
        for (T object : objects) {
            checkAbsent(object);
        }
    }

    @Test
    public void test_01_one() throws DatabaseException {
        T object = generate();
        T id = insert(object);
        checkStored(id);

        delete(id);
        checkAbsent(id);
    }

    @Test
    public void test_02_many() throws DatabaseException {
        ArrayList<T> objects = generate(20);

        List<T> ids = insert(objects);
        checkStored(ids);

        delete(ids);
        checkAbsent(ids);
    }

    private void clean() {
        // Clean posts DB
        cleanTables(postsDatabase, StoredPost.Contract.TABLE_NAME,
                StoredAttachment.Contract.TABLE_NAME, Links.Contract.TABLE_NAME,
                StoredLocation.Contract.TABLE_NAME);
    }

    @Before
    public void setUp() throws Exception {
        clean();
    }

    @After
    public void tearDown() throws Exception {
        clean();
    }
}
