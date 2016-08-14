package ly.loud.loudly.util.database.entities.links;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;

/**
 * Object that represents Links table
 *
 * @author Danil Kolikov
 */
public class Links {
    @Nullable
    Long id;

    @NonNull
    String[] links;

    public Links() {
    }

    public Links(@Nullable Long id, @NonNull String[] links) {
        this.links = links;
        this.id = id;
    }

    @NonNull
    public static Query selectById(long id) {
        return Query.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract._ID + " = ?")
                .whereArgs(id)
                .build();
    }

    @NonNull
    public static DeleteQuery deleteById(long id) {
        return DeleteQuery.builder()
                .table(Contract.TABLE_NAME)
                .where(Contract._ID + " = ?")
                .whereArgs(id)
                .build();
    }

    @NonNull
    public String[] getLinks() {
        return links;
    }

    public void setLinks(@NonNull String[] links) {
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
