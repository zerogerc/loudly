package ly.loud.loudly.application.models;

import android.database.Cursor;
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

import ly.loud.loudly.base.entities.Event;
import ly.loud.loudly.base.entities.Event.EventType;
import ly.loud.loudly.base.entities.Info;
import ly.loud.loudly.base.entities.Location;
import ly.loud.loudly.base.interfaces.MultipleNetworkElement;
import ly.loud.loudly.base.interfaces.SingleNetworkElement;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.multiple.LoudlyImage;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.Networks.Network;
import ly.loud.loudly.util.ListUtils;
import ly.loud.loudly.util.NetworkUtils.DividedList;
import ly.loud.loudly.util.TimeInterval;
import ly.loud.loudly.base.exceptions.DatabaseException;
import ly.loud.loudly.util.database.entities.StoredAttachment;
import ly.loud.loudly.util.database.entities.StoredEvent;
import ly.loud.loudly.util.database.entities.StoredLocation;
import ly.loud.loudly.util.database.entities.StoredPost;
import ly.loud.loudly.util.database.entities.links.Links;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;
import solid.collections.SolidList;

import static ly.loud.loudly.base.entities.Event.COMMENT;
import static ly.loud.loudly.base.entities.Event.LIKE;
import static ly.loud.loudly.base.entities.Event.SHARE;
import static ly.loud.loudly.networks.Networks.LOUDLY;
import static ly.loud.loudly.networks.Networks.NETWORK_COUNT;
import static ly.loud.loudly.util.ListUtils.asSolidList;
import static ly.loud.loudly.util.ListUtils.removeByPredicateInPlace;
import static ly.loud.loudly.util.NetworkUtils.divideListOfCachedPosts;
import static ly.loud.loudly.util.database.entities.StoredEvent.selectByPostId;
import static ly.loud.loudly.util.database.entities.StoredEvent.selectByPostIdAndType;
import static ly.loud.loudly.util.database.entities.StoredEvent.selectByPostIdNetworkAndType;
import static ly.loud.loudly.util.database.entities.StoredEvent.selectOldestEvents;
import static ly.loud.loudly.util.database.entities.StoredEvent.selectTypesByPost;
import static ly.loud.loudly.util.database.entities.StoredEvent.selectTypesByPostIdAndNetwork;
import static solid.collectors.ToList.toList;

public class PostsDatabaseModel {
    @NonNull
    private final StorIOSQLite postsDatabase;

    @NonNull
    private final List<LoudlyPost> cached;

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
    private Completable deleteLinks(long id) {
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
                })
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private <T extends SingleNetworkElement> Completable updateLinks(
            long id,
            @NonNull MultipleNetworkElement<T> element) {
        return putLinks(id, element)
                .map(putResult -> {
                    if (putResult.wasNotUpdated()) {
                        throw Exceptions.propagate(new DatabaseException("Can't update links"));
                    }
                    return true;
                })
                .toCompletable();

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
    private Completable deleteStoredAttachment(@NonNull StoredAttachment attachment) {
        if (attachment.getId() == null) {
            // Can't be deleted
            return Completable.complete();
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
                })
                .toCompletable();
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
                    //noinspection ResourceType i is ID of network
                    elements[i] = new SingleImage(attachment.getExtra(), null, i, links[i]);
                }
                return new LoudlyImage(attachment.getExtra(), null, elements);
        }
        // Now it's impossible
        return null;
    }

    @CheckResult
    @NonNull
    private Completable deleteAttachment(@NonNull StoredAttachment attachment) {
        return deleteLinks(attachment.getLinksId())
                .andThen(deleteStoredAttachment(attachment));
    }

    @CheckResult
    @NonNull
    private Completable updateAttachmentLinks(@NonNull MultipleAttachment attachment) {
        SingleAttachment loudlyInstance = attachment.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            // Can't update links in DB
            return Completable.complete();
        }
        return selectStoredAttachment(Long.parseLong(loudlyInstance.getLink()))
                .flatMap(storedAttachment ->
                        updateLinks(storedAttachment.getLinksId(), attachment)
                                .toSingleDefault(storedAttachment))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Completable updateAttachmentsLinks(
            @NonNull List<MultipleAttachment> attachments
    ) {
        return Observable.from(attachments)
                .flatMap(attachment -> updateAttachmentLinks(attachment).toObservable())
                .toCompletable();
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
                                .map(links -> fromStored(storedAttachment, links))
                                .toObservable())
                .toList()
                .first()
                .toSingle()
                .map(ListUtils::asArrayList);
    }

    @CheckResult
    @NonNull
    private Completable deleteAttachments(long postId) {
        return selectStoredAttachments(postId)
                .flatMap(attachment -> deleteAttachment(attachment).toObservable())
                .toCompletable();
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
    private Completable deleteLocation(@Nullable Long id) {
        if (id == null || id < 1) {
            return Completable.complete();
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
                })
                .toCompletable();

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
    private Completable deleteStoredPost(@NonNull StoredPost storedPost) {
        if (storedPost.getId() == null) {
            return Completable.complete();
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
                })
                .toCompletable();
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

    @CheckResult
    @NonNull
    public Observable<SolidList<LoudlyPost>> selectPostsByIds(@NonNull List<Long> ids) {
        return Observable
                .from(ids)
                .flatMap(id -> selectStoredPost(id).flatMap(this::loadPost).toObservable())
                .cast(LoudlyPost.class)
                .toList()
                .map(ListUtils::asSolidList)
                .first();
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
                })
                .map(post -> {
                    cached.add(0, post);
                    return post;
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
                .flatMap(storedPost -> deleteLinks(storedPost.getLinksId())
                                .andThen(deleteLocation(storedPost.getLocationId()))
                                .andThen(deleteAttachments(storedPost.getId()))
                                .andThen(deleteStoredPost(storedPost))
                                .andThen(deleteStoredEvents(storedPost.getId()))
                                .toSingleDefault(storedPost)
                )
                .toCompletable()
                .andThen(Single.fromCallable(
                        () -> {
                            removeByPredicateInPlace(cached, loudlyPost -> {
                                SingleNetworkElement instance =
                                        loudlyPost.getSingleNetworkInstance(LOUDLY);
                                return instance != null &&
                                        instance.getLink().equals(loudlyInstance.getLink());
                            });
                            return post.deleteNetworkInstance(LOUDLY);
                        }
                ));
    }

    @NonNull
    private LoudlyPost fromStored(@NonNull StoredPost storedPost,
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
                //noinspection ResourceType i is ID of network
                singleAttachments.add(attachment.getSingleNetworkInstance(i));
            }
            //noinspection ResourceType i is ID of network
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
    private Single<LoudlyPost> loadPost(@NonNull StoredPost storedPost) {
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
                )
                        // Load cached likes, comments, ...
                .flatMap(loudlyPost -> Observable
                                .from(loudlyPost.getNetworkInstances())
                                .flatMap(instance -> loadInfo(loudlyPost, instance.getNetwork())
                                                .first()
                                                .map(instance::setInfo)
                                )
                                .reduce(loudlyPost, LoudlyPost::setSingleNetworkInstance)
                                .toSingle()
                );
    }

    @CheckResult
    @NonNull
    private Observable<SolidList<LoudlyPost>> selectPostsByTimeInterval(
            @NonNull TimeInterval timeInterval) {
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
                                        .andThen(updateAttachmentsLinks(loudlyPost.getAttachments()))
                                        .toSingleDefault(loudlyInstance)
                )
                .toCompletable()
                .andThen(Completable.fromAction(() -> {
                    // Update cached post
                    for (int i = 0, size = cached.size(); i < size; i++) {
                        SingleNetworkElement instance = cached.get(i)
                                .getSingleNetworkInstance(LOUDLY);
                        if (instance == null) {
                            continue;
                        }
                        if (instance.getLink().equals(loudlyInstance.getLink())) {
                            cached.set(i, loudlyPost);
                            break;
                        }
                    }
                }))
                .toSingleDefault(loudlyPost);
    }

    @CheckResult
    @NonNull
    public Observable<SolidList<LoudlyPost>> loadPostsByTimeInterval(@NonNull TimeInterval timeInterval) {
        if (cached.isEmpty()) {
            return selectPostsByTimeInterval(timeInterval)
                    .map(list -> {
                        cached.addAll(list);
                        return asSolidList(list);
                    });
        } else {
            final DividedList<LoudlyPost> dividedList =
                    divideListOfCachedPosts(cached, timeInterval);

            return selectPostsByTimeInterval(dividedList.before)
                    .flatMap(before ->
                            selectPostsByTimeInterval(dividedList.after)
                                    .map(after -> {
                                        cached.addAll(before);
                                        cached.addAll(after);
                                        Collections.sort(cached);

                                        List<LoudlyPost> result = new ArrayList<>();
                                        result.addAll(before);
                                        result.addAll(dividedList.cached);
                                        result.addAll(after);
                                        return asSolidList(result);
                                    }));
        }
    }

    @NonNull
    public SolidList<LoudlyPost> getCachedPosts() {
        return asSolidList(cached);
    }

    /* Event part */

    @CheckResult
    @NonNull
    public Completable saveEvents(@NonNull SolidList<Event> events) {
        final List<StoredEvent> storedEvents = events
                .map(event -> {
                    SinglePost loudlyInstance = event.post.getSingleNetworkInstance(LOUDLY);
                    if (loudlyInstance == null) {
                        return null;
                    }
                    return new StoredEvent(
                            null,
                            event.type,
                            event.network,
                            Long.parseLong(loudlyInstance.getLink()),
                            event.date
                    );
                })
                .filter(event -> event != null)
                .collect(toList());

        return saveStoredEvents(storedEvents);
    }

    @CheckResult
    @NonNull
    public Observable<Event> getEvents(@NonNull LoudlyPost loudlyPost) {
        SinglePost loudlyInstance = loudlyPost.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return Observable.empty();
        }
        return getStoredEvents(Long.parseLong(loudlyInstance.getLink()))
                .map(storedEvent -> new Event(
                                storedEvent.getType(),
                                storedEvent.getNetwork(),
                                loudlyPost,
                                storedEvent.getDate()
                        )
                );
    }

    @CheckResult
    @NonNull
    public Observable<Integer> getEventsCount(@NonNull LoudlyPost post,
                                              @EventType short type) {
        SingleNetworkElement loudlyInstance = post.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return Observable.empty();
        }
        return postsDatabase
                .get()
                .numberOfResults()
                .withQuery(selectByPostIdAndType(
                        Long.parseLong(loudlyInstance.getLink()),
                        type
                ))
                .prepare()
                .asRxObservable();
    }

    @CheckResult
    @NonNull
    public Observable<Integer> getEventsCount(@NonNull LoudlyPost post,
                                              @Network int network,
                                              @EventType short type) {
        SingleNetworkElement loudlyInstance = post.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return Observable.empty();
        }
        return postsDatabase
                .get()
                .numberOfResults()
                .withQuery(selectByPostIdNetworkAndType(
                        Long.parseLong(loudlyInstance.getLink()),
                        network,
                        type
                ))
                .prepare()
                .asRxObservable();
    }

    /* Info part */

    @CheckResult
    @NonNull
    public Observable<Info> loadInfo(@NonNull LoudlyPost post, @Network int network) {
        SinglePost loudlyInstance = post.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return Observable.empty();
        }
        return postsDatabase.get()
                .cursor()
                .withQuery(selectTypesByPostIdAndNetwork(
                        Long.parseLong(loudlyInstance.getLink()),
                        network
                ))
                .prepare()
                .asRxObservable()
                .map(this::countInfo);
    }

    @CheckResult
    @NonNull
    public Observable<Info> loadInfo(@NonNull LoudlyPost loudlyPost) {
        SinglePost loudlyInstance = loudlyPost.getSingleNetworkInstance(LOUDLY);
        if (loudlyInstance == null) {
            return Observable.empty();
        }
        return postsDatabase.get()
                .cursor()
                .withQuery(selectTypesByPost(
                        Long.parseLong(loudlyInstance.getLink())
                ))
                .prepare()
                .asRxObservable()
                .map(this::countInfo);
    }

    @CheckResult
    @NonNull
    public Observable<Info> updateStoredInfo(@NonNull LoudlyPost loudlyPost,
                                             @Network int network,
                                             @NonNull Info difference) {
        return updateEvents(loudlyPost, network, LIKE, difference.like)
                .andThen(updateEvents(loudlyPost, network, SHARE, difference.repost))
                .andThen(updateEvents(loudlyPost, network, COMMENT, difference.comment))
                .andThen(Single.fromCallable(
                        () -> {
                            SinglePost loudlyInstance = loudlyPost.getSingleNetworkInstance(LOUDLY);
                            for (int i = 0; i < cached.size(); i++) {
                                SinglePost otherInstance = cached.get(i).getSingleNetworkInstance(LOUDLY);
                                //noinspection ConstantConditions Cached LoudlyPosts have LOUDLY instances
                                if (otherInstance.getLink().equals(loudlyInstance.getLink())) {
                                    SinglePost previous = cached.get(i).getSingleNetworkInstance(network);
                                    if (previous == null) {
                                        continue;
                                    }
                                    Info newInfo = previous.getInfo().add(difference);
                                    cached.set(i, cached.get(i)
                                            .setSingleNetworkInstance(previous.setInfo(newInfo)));
                                }
                            }
                            return difference;
                        }
                ))
                .toObservable();
    }

    @NonNull
    private Info countInfo(@NonNull Cursor cursor) {
        int like = 0;
        int share = 0;
        int comment = 0;
        try {
            if (cursor.moveToFirst()) {
                do {
                    short type = cursor.getShort(
                            cursor.getColumnIndexOrThrow(StoredEvent.Contract.COLUMN_NAME_TYPE)
                    );
                    switch (type) {
                        case LIKE:
                            like++;
                            break;
                        case SHARE:
                            share++;
                            break;
                        case COMMENT:
                            comment++;
                            break;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return new Info(like, share, comment);
    }

    /* StoredEvent part */

    @CheckResult
    @NonNull
    public Observable<Integer> getEventsCount() {
        return postsDatabase
                .get()
                .numberOfResults()
                .withQuery(StoredEvent.selectAll())
                .prepare()
                .asRxObservable();
    }

    @CheckResult
    @NonNull
    private Completable saveStoredEvents(@NonNull List<StoredEvent> events) {
        return postsDatabase
                .put()
                .objects(events)
                .prepare()
                .asRxSingle()
                .map(putResults -> {
                    if (putResults.numberOfInserts() < events.size()) {
                        throw Exceptions.propagate(new DatabaseException("Can't save event"));
                    }
                    return true;
                })
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Completable deleteStoredEvents(@NonNull List<StoredEvent> events) {
        return postsDatabase
                .delete()
                .objects(events)
                .prepare()
                .asRxSingle()
                .map(results -> {
                    for (StoredEvent storedEvent : events) {
                        if (results.wasNotDeleted(storedEvent)) {
                            throw Exceptions.propagate(
                                    new DatabaseException("Can't delete event")
                            );
                        }
                    }
                    return true;
                })
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Completable deleteOldestStoredEvent(@NonNull LoudlyPost post,
                                                @EventType short type,
                                                int count) {
        SinglePost loudlyPost = post.getSingleNetworkInstance(LOUDLY);
        if (loudlyPost == null) {
            return Completable.complete();
        }
        return postsDatabase
                .get()
                .listOfObjects(StoredEvent.class)
                .withQuery(selectOldestEvents(
                        Long.parseLong(loudlyPost.getLink()),
                        type,
                        count
                ))
                .prepare()
                .asRxSingle()
                .flatMap(event -> deleteStoredEvents(event).toSingleDefault(event))
                .toCompletable();
    }

    @CheckResult
    @NonNull
    private Completable updateEvents(@NonNull LoudlyPost post,
                                     @Network int network,
                                     @EventType short type,
                                     int count) {
        if (count == 0) {
            return Completable.complete();
        }
        if (count > 0) {
            List<Event> events = Collections.nCopies(
                    count,
                    new Event(type, network, post, System.currentTimeMillis())
            );
            return saveEvents(asSolidList(events));
        } else {
            return deleteOldestStoredEvent(post, type, -count);
        }
    }

    @CheckResult
    @NonNull
    private Observable<StoredEvent> getStoredEvents(long postId) {
        return postsDatabase
                .get()
                .listOfObjects(StoredEvent.class)
                .withQuery(selectByPostId(postId))
                .prepare()
                .asRxObservable()
                .first()
                .flatMap(Observable::from);
    }

    @CheckResult
    @NonNull
    private Completable deleteStoredEvents(long postId) {
        return postsDatabase
                .delete()
                .byQuery(StoredEvent.deleteByPostId(postId))
                .prepare()
                .asRxSingle()
                .toCompletable();
    }
}