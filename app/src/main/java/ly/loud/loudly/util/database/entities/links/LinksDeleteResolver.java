package ly.loud.loudly.util.database.entities.links;

import android.support.annotation.NonNull;
import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;

/**
 * Delete Resolver for links
 *
 * @author Danil Kolikov
 */
public class LinksDeleteResolver extends DefaultDeleteResolver<Links> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Links object) {
        // Note than ID of links stored in object.links[Networks.LOUDLY]
        return DeleteQuery.builder()
                .table(Links.Contract.TABLE_NAME)
                .where(Links.Contract._ID + " = ?")
                .whereArgs(object.id)
                .build();
    }
}
