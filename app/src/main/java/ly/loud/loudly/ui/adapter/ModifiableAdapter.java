package ly.loud.loudly.ui.adapter;

import java.util.List;

/**
 * Interface for adapters that can modify it's items
 *
 * @param <T> Type of elements in adapter
 * @author Danil Kolikov
 */
public interface ModifiableAdapter<T> {
    /**
     * Update holders for elements from specified lists in UI
     *
     * @param updated List of elements
     */
    void update(List<? extends T> updated);

    /**
     * Insert new elements to the adapter in UI
     *
     * @param inserted List of new elements
     */
    void insert(List<? extends T> inserted);

    /**
     * Delete elements from adapter
     *
     * @param deleted List of deleted elements
     */
    void delete(List<? extends T> deleted);
}
