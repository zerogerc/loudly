package ly.loud.loudly.util.database.entities.links;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import ly.loud.loudly.base.Networks;

/**
 * Object that represents Links table
 *
 * @author Danil Kolikov
 */
public class Links {
    @Nullable
    Long id;

    String[] links;

    public Links() {
    }

    public Links(@Nullable Long id, String[] links) {
        this.links = links;
        this.id = id;
    }

    @Nullable
    public static Long getLoudlyId(Links links) {
        return links.getLinks()[Networks.LOUDLY] == null ? null : Long.parseLong(links.getLinks()[Networks.LOUDLY]);
    }

    /**
     * Select link from DB by ID
     *
     * @param id       ID of links
     * @param database Posts database
     * @return Stored Links, or null, if not found
     */
    @Nullable
    public static Links selectById(long id, @NonNull StorIOSQLite database) {
        return database
                .get()
                .object(Links.class)
                .withQuery(
                        Query.builder()
                                .table(Contract.TABLE_NAME)
                                .where(Contract._ID + " = ?")
                                .whereArgs(id)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    /**
     * Delete links from DB by ID
     * @param id ID of links
     * @param database Posts database
     * @return Result of deletion
     */
    public static DeleteResult deleteById(long id, @NonNull StorIOSQLite database) {
        return database.delete()
                .byQuery(
                        DeleteQuery.builder()
                                .table(Contract.TABLE_NAME)
                                .where(Contract._ID + " = ?")
                                .whereArgs(id)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public String[] getLinks() {
        return links;
    }

    public void setLinks(String[] links) {
        this.links = links;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    public interface Contract extends BaseColumns {
        String TABLE_NAME = "links";

        String COLUMN_NAME_LOUDLY = "loudly"; // For future use
        String COLUMN_NAME_FB = "fb";
        String COLUMN_NAME_TWITTER = "twitter";
        String COLUMN_NAME_INSTAGRAM = "instagram";
        String COLUMN_NAME_VK = "vk";
        String COLUMN_NAME_OK = "ok";
        String COLUMN_NAME_MAIL_RU = "mailru";

        String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_NAME_LOUDLY + " TEXT, "
                        + COLUMN_NAME_FB + " TEXT, "
                        + COLUMN_NAME_TWITTER + " TEXT, "
                        + COLUMN_NAME_INSTAGRAM + " TEXT, "
                        + COLUMN_NAME_VK + " TEXT, "
                        + COLUMN_NAME_OK + " TEXT, "
                        + COLUMN_NAME_MAIL_RU + " TEXT )";

        // SHOULD BE IN THE SAME ORDER AS NETWORKS IN THE "Networks" CLASS
        String[] COLUMNS = {
                COLUMN_NAME_LOUDLY,
                COLUMN_NAME_FB,
                COLUMN_NAME_TWITTER,
                COLUMN_NAME_INSTAGRAM,
                COLUMN_NAME_VK,
                COLUMN_NAME_OK,
                COLUMN_NAME_MAIL_RU
        };
    }
}
