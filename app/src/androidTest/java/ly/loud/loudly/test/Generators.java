package ly.loud.loudly.test;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ly.loud.loudly.base.entities.Link;
import ly.loud.loudly.base.entities.Location;
import ly.loud.loudly.base.multiple.LoudlyImage;
import ly.loud.loudly.base.multiple.LoudlyPost;
import ly.loud.loudly.base.single.SingleImage;
import ly.loud.loudly.base.single.SinglePost;
import ly.loud.loudly.networks.facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.instagram.InstagramKeyKeeper;
import ly.loud.loudly.networks.KeyKeeper;
import ly.loud.loudly.networks.Networks;
import ly.loud.loudly.networks.vk.VKKeyKeeper;
import ly.loud.loudly.base.interfaces.attachments.Attachment;
import ly.loud.loudly.base.interfaces.attachments.MultipleAttachment;
import ly.loud.loudly.base.interfaces.attachments.SingleAttachment;
import ly.loud.loudly.base.plain.PlainPost;
import ly.loud.loudly.util.database.entities.StoredLocation;
import rx.functions.Func0;

import java.util.ArrayList;
import java.util.Random;

/**
 * Some utilities for tests
 *
 * @author Danil Kolikov
 */
public class Generators {
    @NonNull
    public static String randomString(int length, @NonNull Random random) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char)(random.nextInt(26) + 'a');
        }
        return new String(chars);
    }

    @NonNull
    public static Link randomLink(int linkLength, @NonNull Random random) {
        return new Link(randomString(linkLength, random));
    }

    @NonNull
    public static Link[] randomLinks(int maxLinkLength, @NonNull Random random) {
        Link[] links = new Link[Networks.NETWORK_COUNT];
        for (int i = 1; i < links.length; i++) {
            links[0] = random.nextBoolean() ? null : new Link(random.nextLong());
            links[i] = randomLink(random.nextInt(maxLinkLength), random);
        }
        return links;
    }

    @NonNull
    public static SingleImage randomSingleImage(int maxLinkLength, @NonNull Random random) {
        return new SingleImage(randomString(random.nextInt(maxLinkLength), random),
                new Point(random.nextInt(), random.nextInt()),random.nextInt(Networks.NETWORK_COUNT),
                randomLink(random.nextInt(maxLinkLength), random));
    }

    @NonNull
    public static LoudlyImage randomLoudlyImage(int maxLinkLength, @NonNull Random random) {
        LoudlyImage image = new LoudlyImage(randomString(random.nextInt(maxLinkLength), random),
                new Point(random.nextInt(), random.nextInt()));
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (random.nextBoolean()) {
                image = image.setSingleNetworkInstance(i, new SingleImage(image.getUrl(),
                        image.getSize(), i, randomLink(maxLinkLength, random)));
            }
        }
        return image;
    }

    @NonNull
    public static Attachment randomAttachment(int maxLinkLength, @NonNull Random random) {
        if (random.nextBoolean()) {
            return randomLoudlyImage(maxLinkLength, random);
        } else {
            return randomSingleImage(maxLinkLength, random);
        }
    }

    @NonNull
    public static Location randomLocation(int nameSize, @NonNull Random random) {
        return new Location(random.nextDouble(), random.nextDouble(), randomString(nameSize, random));
    }


    @NonNull
    public static StoredLocation randomStoredLocation(int nameSize, @NonNull Random random) {
        return new StoredLocation(randomString(nameSize, random), random.nextDouble(), random.nextDouble());
    }

    @NonNull
    public static <T> ArrayList<T> generateArrayList(int size, @NonNull Func0<T> supplier) {
        ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(supplier.call());
        }
        return result;
    }

    @NonNull
    public static LoudlyPost randomLoudlyPost(int textLength, int locationLength, int attachmentCount, int linkLength,
                                            @NonNull Random random) {
        ArrayList<MultipleAttachment> attachments = generateArrayList(attachmentCount,
                () -> randomLoudlyImage(linkLength, random));
        LoudlyPost post = new LoudlyPost(randomString(textLength, random),
                random.nextLong(),
                attachments,
                randomLocation(locationLength, random));
        for (int i = 0; i < Networks.NETWORK_COUNT; i++) {
            if (random.nextBoolean()) {
                ArrayList<SingleAttachment> attachments1 = new ArrayList<>();
                for (MultipleAttachment attachment : post.getAttachments()) {
                    attachments1.add(attachment.getSingleNetworkInstance(i));
                }
                post = post.setSingleNetworkInstance(i, new SinglePost(post.getText(), post.getDate(),
                        attachments1, post.getLocation(), i, randomLink(linkLength, random)));
            }
        }
        return post;
    }

    @NonNull
    public static SinglePost randomSinglePost(int textLength, int locationLength, int attachmentCount, int linkLength,
                                  @NonNull Random random) {
        return new SinglePost(randomString(textLength, random),
                random.nextLong(),
                generateArrayList(attachmentCount, () -> randomSingleImage(linkLength, random)),
                random.nextBoolean() ? null : randomLocation(locationLength, random),
                random.nextInt(),
                randomLink(linkLength, random));
    }

    @NonNull
    public static PlainPost randomPost(int textLength, int locationLength, int attachmentCount, int linkLength,
                                       @NonNull Random random) {
        if (random.nextBoolean()) {
            return randomLoudlyPost(textLength, locationLength, attachmentCount, linkLength, random);
        } else {
            return randomSinglePost(textLength, locationLength, attachmentCount, linkLength, random);
        }
    }

    @Nullable
    public static KeyKeeper randomKeyKeeper(int network, @NonNull Random random) {
        switch (network) {
            case Networks.FB: {
                FacebookKeyKeeper keyKeeper = new FacebookKeyKeeper();
                keyKeeper.setAccessToken(randomString(20, random));
                return keyKeeper;
            }
            case Networks.VK: {
                VKKeyKeeper keyKeeper = new VKKeyKeeper();
                keyKeeper.setAccessToken(randomString(20, random));
                return keyKeeper;
            }
            case Networks.INSTAGRAM: {
                InstagramKeyKeeper keyKeeper = new InstagramKeyKeeper();
                keyKeeper.setAccessToken(randomString(20, random));
                return keyKeeper;
            }
            default: {
                return null;
            }
        }
    }
}
