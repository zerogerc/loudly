package ly.loud.loudly.networks;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ly.loud.loudly.R;
import ly.loud.loudly.legacy_base.Wrap;
import ly.loud.loudly.networks.facebook.FacebookAuthorizer;
import ly.loud.loudly.networks.facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.facebook.FacebookWrap;
import ly.loud.loudly.networks.instagram.InstagramAuthorizer;
import ly.loud.loudly.networks.instagram.InstagramKeyKeeper;
import ly.loud.loudly.networks.instagram.InstagramWrap;
import ly.loud.loudly.networks.loudly.LoudlyAuthorizer;
import ly.loud.loudly.networks.loudly.LoudlyKeyKeeper;
import ly.loud.loudly.networks.loudly.LoudlyWrap;
import ly.loud.loudly.networks.mail_ru.MailRuAuthoriser;
import ly.loud.loudly.networks.mail_ru.MailRuKeyKeeper;
import ly.loud.loudly.networks.vk.VKAuthorizer;
import ly.loud.loudly.networks.vk.VKKeyKeeper;
import ly.loud.loudly.networks.vk.VKWrap;

/**
 * Constants for getting proper social network for holders
 */
public class Networks {

    public static final int NETWORK_COUNT = 7;

    // It's important to add new network to the LinksContract, so we could save it to DB
    @IntDef
    public @interface Network {
    }

    public static final int LOUDLY = 0;
    public static final int FB = 1;
    public static final int TWITTER = 2;
    public static final int INSTAGRAM = 3;
    public static final int VK = 4;
    public static final int OK = 5;
    public static final int MAILRU = 6;


    @StringRes
    public static int nameResourceOfNetwork(@Network int network) {
        switch (network) {
            case LOUDLY:
                return R.string.network_loudly;
            case FB:
                return R.string.network_facebook;
            case TWITTER:
                return R.string.network_twitter;
            case INSTAGRAM:
                return R.string.network_instagram;
            case VK:
                return R.string.network_vk;
            case OK:
                return R.string.network_ok;
            case MAILRU:
                return R.string.network_myworld;
            default:
                return R.string.network_loudly;
        }
    }

    public static String domainByNetwork(@Network int network) {
        switch (network) {
            case FB:
                return "www.facebook.com";
            case VK:
                return "www.vk.com";
            case INSTAGRAM:
                return "www.instagram.com";
            default:
                return "";
        }
    }

    public static Wrap makeWrap(@Network int network) {
        switch (network) {
            case LOUDLY:
                return new LoudlyWrap();
            case FB:
                return new FacebookWrap();
            case VK:
                return new VKWrap();
            case INSTAGRAM:
                return new InstagramWrap();
            default:
                return null;
        }
    }

    public static Authorizer makeAuthorizer(@Network int network) {
        //TODO other networks
        switch (network) {
            case LOUDLY:
                return new LoudlyAuthorizer();
            case FB:
                return new FacebookAuthorizer();
            case VK:
                return new VKAuthorizer();
            case MAILRU:
                return new MailRuAuthoriser();
            case INSTAGRAM:
                return new InstagramAuthorizer();
            default:
                return null;

        }
    }

    /**
     * Make proper instance of KeyKeeper for the network and fill it with values, stored in DB
     *
     * @param network ID of the network
     * @param strings Fields of this keyKeeper, stored in DB
     * @return KeyKeeper for the network
     */
    public static KeyKeeper makeKeyKeeper(@Network int network, @NonNull String[] strings) {
        switch (network) {
            case LOUDLY:
                return new LoudlyKeyKeeper(strings);
            case FB:
                return new FacebookKeyKeeper(strings);
            case VK:
                return new VKKeyKeeper(strings);
            case INSTAGRAM:
                return new InstagramKeyKeeper(strings);
            case MAILRU:
                return new MailRuKeyKeeper(strings);
            default:
                return null;
        }
    }
}
