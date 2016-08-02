package ly.loud.loudly.util.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import ly.loud.loudly.application.Loudly;
import ly.loud.loudly.new_base.*;
import ly.loud.loudly.new_base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.new_base.interfaces.SingleNetworkElement;
import ly.loud.loudly.new_base.interfaces.attachments.Attachment;
import ly.loud.loudly.new_base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.new_base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.database.entities.Key;
import ly.loud.loudly.util.database.entities.links.Links;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for work with databases
 */
public class DatabaseUtils {
    @NonNull
    static StorIOSQLite getPostsDatabase() {
        return Loudly.getContext().getDatabaseComponent().getPostsDatabase();
    }

    @NonNull
    static StorIOSQLite getKeysDatabase() {
        return Loudly.getContext().getDatabaseComponent().getKeysDatabase();
    }

    static long saveLinks(@NonNull Link[] save) throws DatabaseException {
        Links links = new Links(null, new String[Networks.NETWORK_COUNT]);
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            links.getLinks()[i] = Link.getLink(save[i]);
        }

        PutResult result = getPostsDatabase().put()
                .object(links)
                .prepare()
                .executeAsBlocking();
        Long id = result.insertedId();
        if (id == null) {
            throw new DatabaseException("Links wasn't saved");
        }
        return id;
    }

    @NonNull
    static Link[] loadLinks(long id) throws DatabaseException {
        Links stored = Links.selectById(id, getPostsDatabase());
        if (stored == null) {
            throw new DatabaseException("Can't load links");
        }
        Link[] links = new Link[Networks.NETWORK_COUNT];
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            links[i] = stored.getLinks()[i] == null ? null : new Link(stored.getLinks()[i], false);
        }
        return links;
    }

    static void deleteLinks(long id) throws DatabaseException {
        DeleteResult result = Links.deleteById(id, getPostsDatabase());
        if (result.numberOfRowsDeleted() == 0) {
            throw new DatabaseException("Links wasn't deleted");
        }
    }

    static long saveLocation(@NonNull Location location) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Location persistent =
                new ly.loud.loudly.util.database.entities.Location(location.name, location.latitude, location.longitude);
        persistent.setId(null);
        PutResult result = getPostsDatabase().put()
                .object(persistent)
                .prepare()
                .executeAsBlocking();
        Long id = result.insertedId();
        if (id == null) {
            throw new DatabaseException("Location wasn't saved");
        }
        return id;
    }

    @NonNull
    static Location loadLocation(long id) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Location stored =
                ly.loud.loudly.util.database.entities.Location.selectById(id, getPostsDatabase());
        if (stored == null) {
            throw new DatabaseException("Can't load location");
        }
        return new Location(stored.getLatitude(), stored.getLongitude(), stored.getName());
    }

    static void deleteLocation(long id) throws DatabaseException {
        DeleteResult result = ly.loud.loudly.util.database.entities.Location.deleteById(id, getPostsDatabase());
        if (result.numberOfRowsDeleted() == 0) {
            throw new DatabaseException("Location wasn't deleted");
        }
    }

    @NonNull
    private static Link[] instancesToLinks(MultipleNetworkElement element) {
        Link[] links = new Link[Networks.NETWORK_COUNT];
        @SuppressWarnings("unchecked") // Java doesn't infer that it's an array of SingleNetworkElement
                List<SingleNetworkElement> list = element.getNetworkInstances();
        for (SingleNetworkElement singleNetworkElement : list) {
            links[singleNetworkElement.getNetwork()] = singleNetworkElement.getLink();
        }
        return links;
    }

    static long saveImage(@NonNull LoudlyImage attachment, long postId) throws DatabaseException {
        StorIOSQLite database = getPostsDatabase();
        database.lowLevel().beginTransaction();
        try {

            long linksId = saveLinks(instancesToLinks(attachment));

            ly.loud.loudly.util.database.entities.Attachment persistent =
                    new ly.loud.loudly.util.database.entities.Attachment(
                            null, postId, attachment.getType(), linksId, attachment.getExtra());
            PutResult result = database.put()
                    .object(persistent)
                    .prepare()
                    .executeAsBlocking();
            Long id = result.insertedId();
            if (id == null) {
                throw new DatabaseException("Attachment isn't saved");
            }
            attachment.setSingleNetworkInstance(Networks.LOUDLY, new SingleImage(attachment.getUrl(), attachment.getSize(),
                    Networks.LOUDLY, new Link(id)));

            database.lowLevel().setTransactionSuccessful();
            return id;
        } finally {
            database.lowLevel().endTransaction();
        }
    }

    @NonNull
    static LoudlyImage loadImage(long id) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Attachment stored =
                ly.loud.loudly.util.database.entities.Attachment.selectById(
                        id, getPostsDatabase());
        if (stored == null) {
            throw new DatabaseException("Can't load attachment");
        }
        return finishAttachmentLoading(stored);
    }

    static void deleteAttachment(long id) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Attachment stored =
                ly.loud.loudly.util.database.entities.Attachment.selectById(id, getPostsDatabase());
        if (stored == null) {
            // Was already deleted
            return;
        }
        finishDeletingAttachment(stored);
    }

    @NonNull
    private static ArrayList<MultipleAttachment> loadPostAttachments(long postId) throws DatabaseException {
        List<ly.loud.loudly.util.database.entities.Attachment> stored =
                ly.loud.loudly.util.database.entities.Attachment.selectByPostId(
                        postId, getPostsDatabase());
        ArrayList<MultipleAttachment> result = new ArrayList<>();
        for (ly.loud.loudly.util.database.entities.Attachment attachment : stored) {
            result.add(finishAttachmentLoading(attachment));
        }
        return result;
    }

    private static void deletePostAttachments(long postId) throws DatabaseException {
        List<ly.loud.loudly.util.database.entities.Attachment> stored =
                ly.loud.loudly.util.database.entities.Attachment.selectByPostId(postId, getPostsDatabase());
        for (ly.loud.loudly.util.database.entities.Attachment attachment : stored) {
            finishDeletingAttachment(attachment);
        }
    }

    private static void fillImageFromLinks(@NonNull LoudlyImage image, @NonNull Link[] links) {
        for (int i = 0; i < links.length; i++) {
            if (links[i] != null) {
                image.setSingleNetworkInstance(i, new SingleImage(image.getUrl(), image.getSize(), i, links[i]));
            }
        }
    }

    @NonNull
    private static LoudlyImage finishAttachmentLoading(@NonNull ly.loud.loudly.util.database.entities.Attachment stored)
            throws DatabaseException {
        Link[] links = loadLinks(stored.getLinksId());
        links[Networks.LOUDLY] = new Link(stored.getId());

        LoudlyImage image = new LoudlyImage(stored.getExtra(), null);
        fillImageFromLinks(image, links);
        return image;
    }

    private static void finishDeletingAttachment(@NonNull ly.loud.loudly.util.database.entities.Attachment stored)
            throws DatabaseException {
        StorIOSQLite.LowLevel lowLevel = getPostsDatabase().lowLevel();
        lowLevel.beginTransaction();
        try {
            deleteLinks(stored.getLinksId());

            // Database can't store null ids
            @SuppressWarnings("ConstantConditions")
            long id = stored.getId();

            DeleteResult result = ly.loud.loudly.util.database.entities.Attachment.deleteById(id, getPostsDatabase());
            if (result.numberOfRowsDeleted() == 0) {
                throw new DatabaseException("Can't delete attachment");
            }
            lowLevel.setTransactionSuccessful();
        } finally {
            lowLevel.endTransaction();
        }
    }

    /**
     * Save post to database. All Loudly links in post and attachments will be updated
     *
     * @param post Post for saving
     * @return ID of stored post
     * @throws DatabaseException if anything went wrong with database
     */
    public static long savePost(@NonNull LoudlyPost post) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Post persistent = new ly.loud.loudly.util.database.entities.Post();
        persistent.setText(post.getText());
        persistent.setDate(post.getDate());
        StorIOSQLite database = getPostsDatabase();
        database.lowLevel().beginTransaction();
        try {
            long linksId = saveLinks(instancesToLinks(post));
            persistent.setLinksId(linksId);

            if (post.getLocation() != null) {
                long locationId = saveLocation(post.getLocation());
                persistent.setLocationId(locationId);
            }
            PutResult result = database.put()
                    .object(persistent)
                    .prepare()
                    .executeAsBlocking();
            Long id = result.insertedId();
            if (id == null) {
                throw new DatabaseException("Post wasn't inserted");
            }
            ArrayList<SingleAttachment> attachments = new ArrayList<>();
            for (Attachment attachment : post.getAttachments()) {
                if (attachment instanceof LoudlyImage) {
                    saveImage(((LoudlyImage) attachment), id);
                    attachments.add(((LoudlyImage) attachment).getSingleNetworkInstance(Networks.LOUDLY));
                }
            }
            post.setSingleNetworkInstance(Networks.LOUDLY, new SinglePost(post.getText(), post.getDate(),
                    attachments, post.getLocation(), Networks.LOUDLY, new Link(id)));
            database.lowLevel().setTransactionSuccessful();
            return id;
        } finally {
            database.lowLevel().endTransaction();
        }
    }

    @NonNull
    static LoudlyPost loadPost(long id) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Post stored =
                ly.loud.loudly.util.database.entities.Post.selectById(id, getPostsDatabase());
        if (stored == null) {
            throw new DatabaseException("Can't load post");
        }
        return finishPostLoading(stored);
    }

    private static void fillPostFromLinks(@NonNull LoudlyPost post, @NonNull Link[] links) {
        for (int i = 0; i < links.length; i++) {
            if (links[i] != null) {
                ArrayList<SingleAttachment> attachments = new ArrayList<>();
                for (MultipleAttachment attachment : post.getAttachments()) {
                    attachments.add(attachment.getSingleNetworkInstance(i));
                }
                post.setSingleNetworkInstance(i, new SinglePost(post.getText(), post.getDate(), attachments,
                        post.getLocation(), i, links[i]));
            }
        }
    }

    @NonNull
    private static LoudlyPost finishPostLoading(@NonNull ly.loud.loudly.util.database.entities.Post stored)
            throws DatabaseException {
        Location location = null;
        // Very strange, but sometimes it doesn't return null value (return '0')
        if (stored.getLocationId() != null && stored.getLocationId() != 0) {
            location = loadLocation(stored.getLocationId());
        }

        // Database can't store null ids, so here can't be null
        @SuppressWarnings("ConstantConditions")
        ArrayList<MultipleAttachment> attachments = loadPostAttachments(stored.getId());

        LoudlyPost post = new LoudlyPost(stored.getText(), stored.getDate(), attachments, location);

        Link[] links = loadLinks(stored.getLinksId());
        links[Networks.LOUDLY] = new Link(stored.getId());

        fillPostFromLinks(post, links);

        return post;
    }

    /**
     * Load all posts for specified interval of time
     *
     * @param time Interval of time
     * @return List of posts, may be empty
     * @throws DatabaseException If some error occurs
     */
    @NonNull
    public static List<LoudlyPost> loadPosts(@NonNull TimeInterval time) throws DatabaseException {
        List<ly.loud.loudly.util.database.entities.Post> stored =
                ly.loud.loudly.util.database.entities.Post.selectByTimeInterval(
                        time, getPostsDatabase());
        List<LoudlyPost> result = new ArrayList<>();
        for (ly.loud.loudly.util.database.entities.Post post : stored) {
            result.add(finishPostLoading(post));
        }
        return result;
    }

    static void deletePost(long id) throws DatabaseException {
        ly.loud.loudly.util.database.entities.Post stored =
                ly.loud.loudly.util.database.entities.Post.selectById(id, getPostsDatabase());
        if (stored == null) {
            return;
        }
        StorIOSQLite.LowLevel lowLevel = getPostsDatabase().lowLevel();
        lowLevel.beginTransaction();
        try {
            deleteLinks(stored.getLinksId());
            deletePostAttachments(id);
            // Sometimes id of location is put into DB as '0'
            if (stored.getLocationId() != null && stored.getLocationId() != 0) {
                deleteLocation(stored.getLocationId());
            }
            DeleteResult result = ly.loud.loudly.util.database.entities.Post.deleteById(id, getPostsDatabase());
            if (result.numberOfRowsDeleted() == 0) {
                throw new DatabaseException("Post wasn't deleted");
            }
            lowLevel.setTransactionSuccessful();
        } finally {
            lowLevel.endTransaction();
        }
    }

    /**
     * Delete post from database
     *
     * @param post A post
     * @throws DatabaseException If error with database occurs
     */
    public static void deletePost(@NonNull LoudlyPost post) throws DatabaseException {
        SingleNetworkElement loudlyInstance = post.getSingleNetworkInstance(Networks.LOUDLY);
        if (loudlyInstance == null) {
            // Nothing to delete
            return;
        }
        String loudlyLink = Link.getLink(loudlyInstance.getLink());
        if (loudlyLink == null || loudlyLink.isEmpty()) {
            throw new DatabaseException("Can't delete post with null or empty id");
        }
        long postId = Long.parseLong(loudlyLink);
        deletePost(postId);
    }

    /**
     * Function that saves KeyKeepers to database
     *
     * @throws DatabaseException if anything went wrong with DB
     */
    public static void saveKeys() throws DatabaseException {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            KeyKeeper keyKeeper = Loudly.getContext().getKeyKeeper(i);
            if (keyKeeper != null) {
                keys.add(new Key(i, keyKeeper.toStringBundle()));
            }
        }
        getKeysDatabase()
                .put()
                .objects(keys)
                .prepare()
                .executeAsBlocking();
    }

    /**
     * Function that loads KeyKeepers from DB
     *
     * @throws DatabaseException if anything went wrong with DB
     */
    public static void loadKeys() throws DatabaseException {
        List<Key> keys = Key.selectKeys(getKeysDatabase());
        for (Key key : keys) {
            if (key == null) {
                continue;
            }
            Loudly.getContext().setKeyKeeper(key.getNetwork(),
                    KeyKeeper.fromStringBundle(key.getNetwork(), key.getValue()));
        }
    }

    /**
     * Delete KeyKeeper from DB
     *
     * @param network ID of network, whose key should be deleted
     * @throws DatabaseException if anything went wrong with DB
     */
    public static void deleteKey(int network) throws DatabaseException {
        DeleteResult result = Key.deleteKey(network, getKeysDatabase());
        if (result.numberOfRowsDeleted() == 0) {
            throw new DatabaseException("Can't delete key");
        }
    }

    /**
     * Update key in database
     *
     * @param network   ID of network
     * @param keyKeeper New keykeeper
     * @throws DatabaseException If some error with DB occurs
     */
    public static void updateKey(int network, @Nullable KeyKeeper keyKeeper) throws DatabaseException {
        if (keyKeeper == null) {
            deleteKey(network);
        } else {
            getKeysDatabase()
                    .put()
                    .object(new Key(network, keyKeeper.toStringBundle()))
                    .prepare()
                    .executeAsBlocking();
        }
    }
}
