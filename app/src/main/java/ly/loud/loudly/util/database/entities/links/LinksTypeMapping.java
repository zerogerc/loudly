package ly.loud.loudly.util.database.entities.links;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;

/**
 * Type mapping for Links
 * @author Danil Kolikov
 */
public class LinksTypeMapping extends SQLiteTypeMapping<Links> {
    public LinksTypeMapping() {
        super(new LinksPutResolver(), new LinksGetResolver(), new LinksDeleteResolver());
    }
}
