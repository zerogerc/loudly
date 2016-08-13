package ly.loud.loudly.application.models;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import ly.loud.loudly.base.entities.Location;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.multiple.LoudlyImage;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.NetworkUtils;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.util.database.DatabaseException;
import ly.loud.loudly.util.database.entities.StoredAttachment;
import ly.loud.loudly.util.database.entities.StoredLocation;
import ly.loud.loudly.util.database.entities.StoredPost;
import ly.loud.loudly.util.database.entities.links.Links;
import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;
import solid.collections.SolidList;

import static ly.loud.loudly.networks.Networks.LOUDLY;
import static ly.loud.loudly.networks.Networks.NETWORK_COUNT;
import static ly.loud.loudly.util.ListUtils.asSolidList;

public class PostsDatabaseModel {
    @NonNull
    private final StorIOSQLite postsDatabase;

    @NonNull
    private final List<PlainPost> cached;

    @Inject
    public PostsDatabaseModel(@NonNull @Named("posts") StorIOSQLite postsDatabase) {
        this.postsDatabase = postsDatabase;
        cached = new ArrayList<>();
    }

    /* Links part */

    @NonNull
    private <T extends SingleNetworkElement> String[] instancesToString(
            @NonNull MultipleNetworkElement<T> element) {
        String[] links = new String[Networks.NETWORK_COUNT];
        for (T instance : element.getNetworkInstances()) {
            links[instance.getNetwork()] = instance.getLink();
        }
        return links;
    }

    @CheckResult
    @NonNull
    private <T extends SingleNetworkElement> Single<PutResult> putLinks(
            @Nullable Long linksId,
            @NonNull MultipleNetworkElement<T> element) {
        final String[] links = instancesToString(element);
        return postsDatabase
                .put()
                .object(new Links(linksId, links))
                .prepare()
                .asRxSingle();
    }

    @CheckResult
    @NonNull
    private <T extends SingleNetworkElement> Single<Long> saveLinks(
            @NonNull MultipleNetworkElement<T> element) {
        return putLinks(null, element)
                .map(putResult -> {
                    if (putResult.wasNotInserted()) {
                        throw Exceptions.propagate(new DatabaseException("Can't save links"));
                    }
                    return putResult.insertedId();
                });
    }

    @CheckResult
    @NonNull
    private Single<String[]> loadLinks(long id) {
        return postsDatabase
                .get()
                .object(Links.class)
                .withQuery(Links.selectById(id))
                .prepare()
                .asRxSingle()
                .map(links -> {
                    if (links == null) {
                        throw Exceptions.propagate(new DatabaseException("Can't load links"));
                    }
                    return links.getLinks();
                });
    }

    @CheckResult
    @NonNull
    private Single<Boolean> deleteLinks(long id) {
        return postsDatabase
                .delete()
                .byQuery(Links.deleteById(id))
                .prepare()
                .asRxSingle()
                .map(deleteResult -> {
                    if (deleteResult.numberOfRowsDeleted() == 0) {
                        throw Exceptions.propagate(new DatabaseException("Can't delete links"));
                    }
                    return true;
                });
    }

    @CheckResult
    @NonNull
    private <T extends SingleNetworkElement> Single<Boolean> updateLinks(
            long id,
            @NonNull MultipleNetworkElement<T> element) {
        return putLinks(id, element)
                .map(putResult -> {
                    if (putResult.wasNotUpdated()) {
                        throw Exceptions.propagate(new DatabaseException("Can't update links"));
                    }
                    return true;
                });

    }

    /* StoredAttachment part */

    @CheckResult
    @NonNull
    private Single<Long> saveStoredAttachment(@NonNull StoredAttachment attachment) {
        return postsDatabase
                .put()
                .object(attachment)
                .prepare()
                .asRxSingle()
                .map(putResult -> {
                    if (putResult.wasNotInserted()) {
                        throw Exceptions.propagate(new DatabaseException("Can't save attachment"));
                    }
                    return putResult.insertedId();
                });
    }

    @CheckResult
    @NonNull
    private Single<Boolean> deleteStoredAttachment(@NonNull StoredAttachment attachment) {
        if (attachment.getId() == null) {
            // Can't be deleted
            return Single.just(false);
        }
        return postsDatabase
                .delete()
                .byQuery(StoredAttachment.deleteById(attachment.getId()))
                .prepare()
                .asRxSingle()
                .map(deleteResult -> {
                    if (deleteResult.numberOfRowsDeleted() == 0) {
                        throw Exceptions.propagate(new DatabaseException("Can't delete attachment"));
                    }
                    return true;
                });
    }

    @CheckResult
    @NonNull
    private Single<StoredAttachment> selectStoredAttachment(long id) {
        return postsDatabase
                .get()
                .object(StoredAttachment.class)
                .withQuery(StoredAttachment.selectById(id))
                .prepare()
                .asRxSingle()
                .map(storedAttachment -> {
                    if (storedAttachment == null) {
                        throw Exceptions.propagate(new DatabaseException("Can't load attachment"));
                    }
                    return storedAttachment;
                });
    }

    @CheckResult
    @NonNull
    private Observable<StoredAttachment> selectStoredAttachments(long postId) {
        return postsDatabase
                .get()
                .listOfObjects(StoredAttachment.class)
                .withQuery(StoredAttachment.selectByPostId(postId))
                .prepare()
                .asRxSingle()
                .flatMapObservable(Observable::from);
    }

    /* Multiple attachment part */

    @CheckResult
    @NonNull
    private Single<SingleAttachment> putAttachment(
            @NonNull MultipleAttachment attachment,
            long postId
    ) {
        final StoredAttachment storedAttachment = new StoredAttachment()
                .setId(null)
                .setPostId(postId)
                .setType(attachment.getType())
                .setExtra(attachment.getExtra());

        return saveLinks(attachment)
                .map(storedAttachment::setLinksId)
                .flatMap(this::saveStoredAttachment)
                .map(id -> {
                    if (attachment instanceof LoudlyImage) {
                        return new SingleImage(attachment.getExtra(), null,
                                LOUDLY, Long.toString(id));
                    }
                    // Impossible case now
                    throw Exceptions.propagate(
                            new IllegalArgumentException("Wrong type of attachment")
                    );
                });
    }

    @Nullable
    private MultipleAttachment fromStored(
            @NonNull StoredAttachment attachment,
            @NonNull String[] links
    ) {
        switch (attachment.getType()) {
            case Attachment.TYPE_IMAGE:
                SingleImage[] elements = new SingleImage[NETWORK_COUNT];
                //noinspection ConstantConditions StoredAttachment was loaded from DB, so it has ID
                links[LOUDLY] = Long.toString(attachment.getId());
                for (int i = 0; i < NETWORK_COUNT; i++) {
                    if (links[i] == null) {
                        continue;
                    }
                    elements[i] = new SingleImage(attachment.getExtra(), null, i, links[i]);
                }
                return new LoudlyImage(attachment.getExtra(), null, elements);
        }
        // Now it's impossible
        return null;
    }

    @CheckResult
    @NonNull
    private Single<Boolean> deleteAttachment(@NonNull StoredAttachment attachment) {
        return deleteLinks(attachment.getLinksId())
                .flatMap(ignored -> deleteStoredAttachment(attachment));
    }

    @CheckResult
    @NonNull
    private Single<Boolean> updateAttachmentLinks(@NonNull MultipleAttachment attachment) {
        SingleAttachment loudlyInstance = attachment.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            // Can't update links in DB
            return Single.just(false);
        }
        return selectStoredAttachment(Long.parseLong(loudlyInstance.getLink()))
                .flatMap(storedAttachment -> updateLinks(storedAttachment.getLinksId(), attachment));
    }

    @CheckResult
    @NonNull
    private Single<List<Boolean>> updateAttachmentsLinks(
            @NonNull List<MultipleAttachment> attachments
    ) {
        return Observable.from(attachments)
                .flatMap(attachment -> updateAttachmentLinks(attachment).toObservable())
                .toList()
                .first()
                .toSingle();
    }

    @CheckResult
    @NonNull
    private Single<ArrayList<SingleAttachment>> putAttachments(
            @NonNull List<MultipleAttachment> attachments,
            long postId
    ) {
        return Observable.from(attachments)
                .flatMap(attachment -> putAttachment(attachment, postId).toObservable())
                .toList()
                .map(ListUtils::asArrayList)
                .first()
                .toSingle();
    }

    @CheckResult
    @NonNull
    private Single<ArrayList<MultipleAttachment>> loadAttachments(long postId) {
        return selectStoredAttachments(postId)
                .flatMap(storedAttachment ->
                        loadLinks(storedAttachment.getLinksId())
                                .map(links ->
                                        fromStored(storedAttachment, links))
                                .toObservable())
                .toList()
                .first()
                .toSingle()
                .map(ListUtils::asArrayList);
    }

    @CheckResult
    @NonNull
    private Single<List<Boolean>> deleteAttachments(long postId) {
        return selectStoredAttachments(postId)
                .flatMap(attachment -> deleteAttachment(attachment).toObservable())
                .toList()
                .first()
                .toSingle();
    }

    /* Locations part */

    @CheckResult
    @NonNull
    private Single<Long> saveLocation(@Nullable Location location) {
        if (location == null) {
            return Single.just(null);
        }
        return postsDatabase
                .put()
                .object(new StoredLocation(null, location.name, location.latitude, location.longitude))
                .prepare()
                .asRxSingle()
                .map(putResult -> {
                    if (!putResult.wasInserted()) {
                        throw Exceptions.propagate(new DatabaseException("Can't save location"));
                    }
                    return putResult.insertedId();
                });
    }

    @CheckResult
    @NonNull
    private Single<Boolean> deleteLocation(@Nullable Long id) {
        if (id == null || id < 1) {
            return Single.just(false);
        }
        return postsDatabase
                .delete()
                .byQuery(StoredLocation.deleteById(id))
                .prepare()
                .asRxSingle()
                .map(deleteResult -> {
                    if (deleteResult.numberOfRowsDeleted() == 0) {
                        throw Exceptions.propagate(new DatabaseException("Can't delete location"));
                    }
                    return true;
                });

    }

    @CheckResult
    @NonNull
    private Single<Location> loadLocation(@Nullable Long id) {
        if (id == null || id < 1) {
            return Single.just(null);
        }
        return postsDatabase
                .get()
                .object(StoredLocation.class)
                .withQuery(StoredLocation.selectById(id))
                .prepare()
                .asRxSingle()
                .map(storedLocation -> {
                    if (storedLocation == null) {
                        throw Exceptions.propagate(new DatabaseException("Can't load location"));
                    }
                    return new Location(
                            storedLocation.getLatitude(),
                            storedLocation.getLongitude(),
                            storedLocation.getName()
                    );
                });
    }

    /* Stored post part */

    @CheckResult
    @NonNull
    private Single<Long> putStoredPost(@NonNull StoredPost storedPost) {
        return postsDatabase
                .put()
                .object(storedPost)
                .prepare()
                .asRxSingle()
                .map(putResult -> {
                    if (!putResult.wasInserted()) {
                        throw Exceptions.propagate(new DatabaseException("Can't save post"));
                    }
                    return putResult.insertedId();
                });
    }

    @CheckResult
    @NonNull
    private Single<Boolean> deleteStoredPost(@NonNull StoredPost storedPost) {
        if (storedPost.getId() == null) {
            // Can't be deleted
            return Single.just(false);
        }
        return postsDatabase
                .delete()
                .byQuery(StoredPost.deleteById(storedPost.getId()))
                .prepare()
                .asRxSingle()
                .map(deleteResult -> {
                    if (deleteResult.numberOfRowsDeleted() == 0) {
                        throw Exceptions.propagate(new DatabaseException("Can't delete post"));
                    }
                    return true;
                });
    }

    @CheckResult
    @NonNull
    private Single<StoredPost> selectStoredPost(long postId) {
        return postsDatabase
                .get()
                .object(StoredPost.class)
                .withQuery(StoredPost.selectById(postId))
                .prepare()
                .asRxSingle()
                .map(storedPost -> {
                    if (storedPost == null) {
                        throw Exceptions.propagate(new DatabaseException("Can't load post"));
                    }
                    return storedPost;
                });
    }

    @CheckResult
    @NonNull
    private Observable<StoredPost> selectStoredPostsByTimeInterval(@NonNull TimeInterval interval) {
        return postsDatabase
                .get()
                .listOfObjects(StoredPost.class)
                .withQuery(StoredPost.selectByTimeInterval(interval))
                .prepare()
                .asRxSingle()
                .flatMapObservable(Observable::from);
    }

    /* LoudlyPost part */

    @CheckResult
    @NonNull
    public Single<LoudlyPost> putPost(@NonNull LoudlyPost loudlyPost) {
        final StoredPost storedPost = new StoredPost();
        storedPost
                .setId(null)
                .setText(loudlyPost.getText())
                .setDate(loudlyPost.getDate());

        //noinspection ConstantConditions Post will be stored in DB, so it will have ID
        return saveLinks(loudlyPost)
                .map(storedPost::setLinksId)
                .flatMap(post -> saveLocation(loudlyPost.getLocation()))
                .map(storedPost::setLocationId)
                .flatMap(this::putStoredPost)
                .map(storedPost::setId)
                .flatMap(post -> putAttachments(loudlyPost.getAttachments(), post.getId()))
                .map(attachments -> {
                    //noinspection ConstantConditions Post is stored, so it has ID
                    SinglePost loudlyInstance = new SinglePost(
                            storedPost.getText(), storedPost.getDate(), attachments,
                            loudlyPost.getLocation(), LOUDLY, Long.toString(storedPost.getId())
                    );
                    return loudlyPost.setSingleNetworkInstance(loudlyInstance);
                });
    }

    @CheckResult
    @NonNull
    public Single<LoudlyPost> deletePost(@NonNull LoudlyPost post) {
        SinglePost loudlyInstance = post.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            // Have no post - nothing to delete
            return Single.just(post);
        }
        //noinspection ConstantConditions StoredPost will have ID, because it's stored
        return selectStoredPost(Long.parseLong(loudlyInstance.getLink()))
                .flatMap(storedPost -> Single.just(storedPost.getLinksId())
                                .flatMap(this::deleteLinks)
                                .flatMap(ignored -> deleteLocation(storedPost.getLocationId()))
                                .flatMap(ignored -> deleteAttachments(storedPost.getId()))
                                .flatMap(ignored -> deleteStoredPost(storedPost))
                )
                .map(result -> post.deleteNetworkInstance(LOUDLY));
    }

    @NonNull
    private PlainPost fromStored(@NonNull StoredPost storedPost,
                                 @NonNull String[] links,
                                 @Nullable Location location,
                                 @NonNull ArrayList<MultipleAttachment> attachments) {
        SinglePost[] elements = new SinglePost[NETWORK_COUNT];
        //noinspection ConstantConditions StoredPost was loaded from DB, so has ID
        links[LOUDLY] = Long.toString(storedPost.getId());

        for (int i = 0; i < links.length; i++) {
            if (links[i] == null) {
                continue;
            }
            ArrayList<SingleAttachment> singleAttachments = new ArrayList<>();
            for (MultipleAttachment attachment : attachments) {
                singleAttachments.add(attachment.getSingleNetworkInstance(i));
            }
            elements[i] = new SinglePost(
                    storedPost.getText(),
                    storedPost.getDate(),
                    singleAttachments,
                    location,
                    i,
                    links[i]
            );
        }
        return new LoudlyPost(
                storedPost.getText(),
                storedPost.getDate(),
                attachments,
                location,
                elements
        );
    }

    @CheckResult
    @NonNull
    private Single<PlainPost> loadPost(@NonNull StoredPost storedPost) {
        //noinspection ConstantConditions StoredPost have ID, because it's loaded from DB
        return loadLinks(storedPost.getLinksId())
                .flatMap(links -> loadLocation(storedPost.getLocationId())
                                .flatMap(location -> loadAttachments(storedPost.getId())
                                                .map(multipleAttachments ->
                                                                fromStored(
                                                                        storedPost,
                                                                        links,
                                                                        location,
                                                                        multipleAttachments
                                                                )
                                                )
                                )
                );
    }

    @CheckResult
    @NonNull
    private Observable<SolidList<PlainPost>> selectPostsByTimeInterval(@NonNull TimeInterval timeInterval) {
        return selectStoredPostsByTimeInterval(timeInterval)
                .flatMap(storedPost -> loadPost(storedPost).toObservable())
                .toList()
                .map(ListUtils::asSolidList);
    }

    @CheckResult
    @NonNull
    public Single<LoudlyPost> updatePostLinks(@NonNull LoudlyPost loudlyPost) {
        SingleNetworkElement loudlyInstance = loudlyPost.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            // Can't update links
            return Single.just(loudlyPost);
        }
        return selectStoredPost(Long.parseLong(loudlyInstance.getLink()))
                .flatMap(storedPost ->
                        updateLinks(storedPost.getLinksId(), loudlyPost)
                                .flatMap(ignored -> updateAttachmentsLinks(loudlyPost.getAttachments())))
                .map(ignored -> loudlyPost);
    }

    public Observable<SolidList<PlainPost>> loadPostsByTimeInterval(@NonNull TimeInterval timeInterval) {
        if (cached.isEmpty()) {
            return selectPostsByTimeInterval(timeInterval)
                    .map(list -> {
                        cached.addAll(list);
                        return asSolidList(list);
                    });
        } else {
            final NetworkUtils.DividedList<PlainPost> dividedList =
                    NetworkUtils.divideListOfCachedPosts(cached, timeInterval);

            return selectPostsByTimeInterval(dividedList.before)
                    .flatMap(before ->
                            selectPostsByTimeInterval(dividedList.after)
                                    .map(after -> {
                                        cached.addAll(before);
                                        cached.addAll(after);
                                        Collections.sort(cached);

                                        List<PlainPost> result = new ArrayList<>();
                                        result.addAll(before);
                                        result.addAll(dividedList.cached);
                                        result.addAll(after);
                                        return asSolidList(result);
                                    }));
        }
    }

    @NonNull
    public SolidList<PlainPost> getCachedPosts() {
        return asSolidList(cached);
    }
}