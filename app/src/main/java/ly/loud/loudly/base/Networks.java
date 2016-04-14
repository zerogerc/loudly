package ly.loud.loudly.base;

import ly.loud.loudly.networks.Facebook.FacebookAuthorizer;
import ly.loud.loudly.networks.Facebook.FacebookKeyKeeper;
import ly.loud.loudly.networks.Facebook.FacebookWrap;
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

    // It's important to add new network to the LinksContract, so we could say it in DB
    public static final int LOUDLY = 0;
    public static final int FB = 1;
    public static final int TWITTER = 2;
    public static final int INSTAGRAM = 3;
    public static final int VK = 4;
    public static final int OK = 5;
    public static final int MAILRU = 6;

    public static final int NETWORK_COUNT = 7;

    public static String nameOfNetwork(int network) {
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

    public static String domainByNetwork(int network) {
        switch (network) {
            case FB:
                return "www.facebook.com";
            case VK:
                return "www.vk.com";
            default:
                return "";
        }
    }

    public static Wrap makeWrap(int network) {
        switch (network) {
            case LOUDLY:
                return new LoudlyWrap();
            case FB:
                return new FacebookWrap();
            case VK:
                return new VKWrap();
            default:
                return null;
        }
    }

    public static Authorizer makeAuthorizer(int network) {
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
            default:
                return null;

        }
    }

    /**
     * Make proper instance of KeyKeeper for the network
     * @param network ID of the network
     * @return KeyKeeper for the network
     */
    public static KeyKeeper makeKeyKeeper(int network) {
        switch (network) {
            case LOUDLY:
                return new LoudlyKeyKeeper();
            case FB:
                return new FacebookKeyKeeper();
            case VK:
                return new VKKeyKeeper();
            case MAILRU:
                return new MailRuKeyKeeper();
            default:
                return null;
        }
    }
}
