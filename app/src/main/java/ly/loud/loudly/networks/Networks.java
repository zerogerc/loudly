package ly.loud.loudly.networks;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ly.loud.loudly.R;
import ly.loud.loudly.networks.facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.instagram.InstagramKeyKeeper;
import ly.loud.loudly.networks.vk.VKKeyKeeper;

/**
 * Constants for getting proper social network for holders
 */
public class Networks {

    public static final int NETWORK_COUNT = 7;

    // It's important to add new network to the LinksContract, so we could save it to DB
    @IntDef({LOUDLY, FB, TWITTER, INSTAGRAM, VK, OK, MAILRU})
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

    /**
     * Make proper instance of KeyKeeper for the network and fill it with values, stored in DB
     *
     * @param network ID of the network
     * @param strings Fields of this keyKeeper, stored in DB
     * @return KeyKeeper for the network
     */
    public static KeyKeeper makeKeyKeeper(@Network int network, @NonNull String[] strings) {
        switch (network) {
            case FB:
                return new FacebookKeyKeeper(strings);
            case VK:
                return new VKKeyKeeper(strings);
            case INSTAGRAM:
                return new InstagramKeyKeeper(strings);
            case MAILRU:
                return null;
            default:
                return null;
        }
    }
}
