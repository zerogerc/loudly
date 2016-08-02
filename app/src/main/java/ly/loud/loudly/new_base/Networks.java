package ly.loud.loudly.new_base;

import android.support.annotation.IntDef;

import ly.loud.loudly.base.Wrap;
import ly.loud.loudly.networks.Facebook.FacebookAuthorizer;
import ly.loud.loudly.networks.Facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.Facebook.FacebookWrap;
import ly.loud.loudly.networks.Instagram.InstagramAuthorizer;
import ly.loud.loudly.networks.Instagram.InstagramKeyKeeper;
import ly.loud.loudly.networks.Instagram.InstagramWrap;
import ly.loud.loudly.networks.Loudly.LoudlyAuthorizer;
import ly.loud.loudly.networks.Loudly.LoudlyKeyKeeper;
import ly.loud.loudly.networks.Loudly.LoudlyWrap;
import ly.loud.loudly.networks.MailRu.MailRuAuthoriser;
import ly.loud.loudly.networks.MailRu.MailRuKeyKeeper;
import ly.loud.loudly.networks.VK.VKAuthorizer;
import ly.loud.loudly.networks.VK.VKKeyKeeper;
import ly.loud.loudly.networks.VK.VKWrap;

/**
 * Constants for getting proper social network for holders
 */
public class Networks {

    public static final int NETWORK_COUNT = 7;

    // It's important to add new network to the LinksContract, so we could save it to DB
    @IntDef
    public @interface Network {}
    public static final int LOUDLY = 0;
    public static final int FB = 1;
    public static final int TWITTER = 2;
    public static final int INSTAGRAM = 3;
    public static final int VK = 4;
    public static final int OK = 5;
    public static final int MAILRU = 6;


    public static String nameOfNetwork(@Network int network) {
        switch (network) {
            case LOUDLY:
                return "Loudly";
            case FB:
                return "Facebook";
            case TWITTER:
                return "Twitter";
            case INSTAGRAM:
                return "Instagram";
            case VK:
                return "ВКонтакте";
            case OK:
                return "Одноклассники";
            case MAILRU:
                return "Мой мир";
            default:
                return "";
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
     * Make proper instance of KeyKeeper for the network
     * @param network ID of the network
     * @return KeyKeeper for the network
     */
    public static KeyKeeper makeKeyKeeper(@Network int network) {
        switch (network) {
            case LOUDLY:
                return new LoudlyKeyKeeper();
            case FB:
                return new FacebookKeyKeeper();
            case VK:
                return new VKKeyKeeper();
            case INSTAGRAM:
                return new InstagramKeyKeeper();
            case MAILRU:
                return new MailRuKeyKeeper();
            default:
                return null;
        }
    }
}
