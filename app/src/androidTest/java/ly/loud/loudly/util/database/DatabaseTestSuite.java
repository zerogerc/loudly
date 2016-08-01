package ly.loud.loudly.util.database;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for database
 *
 * @author Danil Kolikov
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({LocationTest.class, LinksTest.class, AttachmentTest.class, PostTest.class,
        KeyTest.class})
public class DatabaseTestSuite {
}
