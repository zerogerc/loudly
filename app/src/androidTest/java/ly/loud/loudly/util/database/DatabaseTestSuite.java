package ly.loud.loudly.util.database;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for database
 *
 * @author Danil Kolikov
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({StoredLocationTest.class, StoredPostTest.class,
        KeyTest.class})
public class DatabaseTestSuite {
}
