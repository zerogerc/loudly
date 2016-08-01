package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ly.loud.loudly.new_base.LoudlyImage;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;
import ly.loud.loudly.util.Equality;
import ly.loud.loudly.test.Generators;
import org.junit.Assert;

/**
 * @author Danil Kolikov
 */
public class AttachmentTest extends DatabaseTest<ly.loud.loudly.new_base.interfaces
        .attachments.Attachment> {
    @NonNull
    @Override
    protected Attachment generate() {
        return Generators.randomLoudlyImage(20, random);
    }

    @NonNull
    @Override
    protected Attachment get(long id) throws DatabaseException {
        return DatabaseUtils.loadImage(id);
    }

    @Override
    protected void delete(long id) throws DatabaseException {
        DatabaseUtils.deleteAttachment(id);
    }

    @Override
    protected long insert(@NonNull Attachment object) throws DatabaseException {
        Assert.assertTrue(object instanceof LoudlyImage);
        return DatabaseUtils.saveImage((LoudlyImage) object, 1234567890L);
    }

    @Override
    protected boolean equals(@Nullable Attachment a, @Nullable Attachment b) {
        return Equality.equal(a, b);
    }
}