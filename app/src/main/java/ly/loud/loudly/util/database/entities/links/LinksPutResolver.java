package ly.loud.loudly.util.database.entities.links;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery;

/**
 * Put resolver for Links
 *
 * @author Danil Kolikov
 */
public class LinksPutResolver extends DefaultPutResolver<Links> {
    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull Links object) {
        return InsertQuery.builder()
                .table(Links.Contract.TABLE_NAME)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull Links object) {
        return UpdateQuery.builder()
                .table(Links.Contract.TABLE_NAME)
                .where(Links.Contract._ID + " = ?")
                .whereArgs(object.id)
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull Links object) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < Links.Contract.COLUMNS.length; i++) {
            contentValues.put(Links.Contract.COLUMNS[i], object.links[i]);
        }
        return contentValues;
    }
}
