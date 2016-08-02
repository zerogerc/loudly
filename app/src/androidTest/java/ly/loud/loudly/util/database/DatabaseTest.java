package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import ly.loud.loudly.test.Generators;
import ly.loud.loudly.util.database.entities.Attachment;
import ly.loud.loudly.util.database.entities.Post;
import ly.loud.loudly.util.database.entities.links.Links;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

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

    @NonNull
    protected abstract T generate();

    @NonNull
    protected abstract T get(long id) throws DatabaseException;

    protected abstract void delete(long id) throws DatabaseException;

    protected abstract boolean equals(@Nullable T a, @Nullable T b);

    protected abstract long insert(@NonNull T object) throws DatabaseException;

    private void checkStored(long id, @NonNull T object) throws DatabaseException {
        T stored = get(id);
        assertTrue(equals(stored, object));
    }

    private void checkAbsent(long id) {
        try {
            get(id);
            throw new AssertionError("Post wasn't deleted");
        } catch (DatabaseException e) {
            // That's fine
        }
    }

    @NonNull
    private ArrayList<T> generate(int size) {
        return Generators.generateArrayList(size, this::generate);
    }

    @NonNull
    private List<Long> insert(@NonNull List<T> objects) throws DatabaseException {
        List<Long> ids = new ArrayList<>();
        for (T object : objects) {
            ids.add(insert(object));
        }
        return ids;
    }

    private void delete(@NonNull List<Long> ids) throws DatabaseException {
        for (int i = 0; i < ids.size(); i++) {
            delete(ids.get(i));
        }
    }

    private void checkStored(@NonNull List<Long> ids, @NonNull List<T> objects) throws DatabaseException {
        assertEquals(ids.size(), objects.size());
        for (int i = 0; i < objects.size(); i++) {
            checkStored(ids.get(i), objects.get(i));
        }
    }

    private void checkAbsent(@NonNull List<Long> ids) {
        for (Long id : ids) {
            checkAbsent(id);
        }
    }

    @Test
    public void test_01_one() throws DatabaseException {
        T object = generate();
        long id = insert(object);
        checkStored(id, object);

        delete(id);
        checkAbsent(id);
    }

    @Test
    public void test_02_many() throws DatabaseException {
        ArrayList<T> objects = generate(20);

        List<Long> ids = insert(objects);
        checkStored(ids, objects);

        delete(ids);
        checkAbsent(ids);
    }

    private void clean() {
        // Clean posts DB
        cleanTables(postsDatabase, Post.Contract.TABLE_NAME,
                Attachment.Contract.TABLE_NAME, Links.Contract.TABLE_NAME,
                ly.loud.loudly.util.database.entities.Location.Contract.TABLE_NAME);
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
