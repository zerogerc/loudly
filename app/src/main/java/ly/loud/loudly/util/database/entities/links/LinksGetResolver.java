package ly.loud.loudly.util.database.entities.links;

import android.database.Cursor;
import android.support.annotation.NonNull;
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver;
import ly.loud.loudly.new_base.Networks;

/**
 * Get resolver for Links
 *
 * @author Danil Kolikov
 */
public class LinksGetResolver extends DefaultGetResolver<Links> {
    @NonNull
    @Override
    public Links mapFromCursor(@NonNull Cursor cursor) {
        String[] strings = new String[Networks.NETWORK_COUNT];

        // Note that link to Loudly is stored in _ID
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(Links.Contract._ID));

        for (int i = 0; i < Links.Contract.COLUMNS.length; i++) {
            strings[i] = cursor.getString(cursor.getColumnIndexOrThrow(Links.Contract.COLUMNS[i]));
        }
        return new Links(id, strings);
    }
}
