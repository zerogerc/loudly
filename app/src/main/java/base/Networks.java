package base;

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
}
