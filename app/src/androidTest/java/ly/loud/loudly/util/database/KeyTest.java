package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.Equality;
import ly.loud.loudly.test.Generators;
import ly.loud.loudly.util.database.entities.Key;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Danil Kolikov
 */
@RunWith(AndroidJUnit4.class)
public class KeyTest {
    @NonNull
    private StorIOSQLite keysDatabase = DatabaseUtils.getKeysDatabase();

    @NonNull
    private Random random = new Random(1234567890L);

    private static void checkKeys(@NonNull List<KeyKeeper> keys) {
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            KeyKeeper saved = Loudly.getContext().getKeyKeeper(i);
            KeyKeeper expected = keys.get(i);
            if (saved == null && expected == null) {
                continue;
            }
            if (saved != null && expected != null) {
                Assert.assertTrue(Equality.equal(saved.toStringBundle(), expected.toStringBundle()));
                continue;
            }
            throw new AssertionError("Not equal for " + i);
        }
    }

    @Before
    public void setUp() {
        DatabaseTest.cleanTables(keysDatabase, Key.Contract.TABLE_NAME);
    }

    @NonNull
    private List<KeyKeeper> initRandomKeys() {
        List<KeyKeeper> result = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            KeyKeeper keyKeeper = Generators.randomKeyKeeper(i, random);
            Loudly.getContext().setKeyKeeper(i, keyKeeper);
            result.add(keyKeeper);
        }
        return result;
    }

    @Test
    public void testStore() throws DatabaseException {
        List<KeyKeeper> keys = initRandomKeys();
        DatabaseUtils.saveKeys();

        DatabaseUtils.loadKeys();
        checkKeys(keys);

        List<KeyKeeper> next = initRandomKeys();
        DatabaseUtils.saveKeys();

        DatabaseUtils.loadKeys();
        checkKeys(next);
    }

    @Test
    public void testUpdateKey() throws DatabaseException {
        List<KeyKeeper> keys = initRandomKeys();
        DatabaseUtils.saveKeys();

        KeyKeeper keyKeeper = Generators.randomKeyKeeper(Networks.VK, random);
        keys.set(Networks.VK, keyKeeper);
        Loudly.getContext().setKeyKeeper(Networks.VK, keyKeeper);
        DatabaseUtils.updateKey(Networks.VK, keyKeeper);

        DatabaseUtils.loadKeys();
        checkKeys(keys);
    }

    @Test
    public void testDeleteKey() throws DatabaseException {
        List<KeyKeeper> keys = initRandomKeys();
        DatabaseUtils.saveKeys();

        DatabaseUtils.deleteKey(Networks.VK);
        keys.set(Networks.VK, null);
        Loudly.getContext().setKeyKeeper(Networks.VK, null);

        DatabaseUtils.loadKeys();
        checkKeys(keys);
    }

    @After
    public void tearDown() {
        DatabaseTest.cleanTables(keysDatabase, Key.Contract.TABLE_NAME);
    }
}
