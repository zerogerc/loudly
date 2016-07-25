package ly.loud.loudly.networks.VK.entities;

import java.util.List;

/**
 * Item container for VK api
 * @author Danil Kolikov
 */
public class VKItems<T> {
    Long count;
    List<T> items;
    List<Profile> profiles;
}
