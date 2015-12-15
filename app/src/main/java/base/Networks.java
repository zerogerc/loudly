package base;

import Facebook.FacebookAuthorizer;
import Facebook.FacebookKeyKeeper;
import Facebook.FacebookWrap;
import MailRu.MailRuAuthoriser;
import MailRu.MailRuKeyKeeper;
import VK.VKAuthorizer;
import VK.VKKeyKeeper;
import VK.VKWrap;

/**
 * Constants for getting proper social network for holders
 */
public class Networks {
    public static final int FB = 0;
    public static final int TWITTER = 1;
    public static final int INSTAGRAM = 2;
    public static final int VK = 3;
    public static final int OK = 4;
    public static final int MAILRU = 5;

    public static final int NETWORK_COUNT = 6;

    public static String nameOfNetwork(int network) {
        switch (network) {
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

    public static Wrap makeWrap(int network) {
        switch (network) {
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
