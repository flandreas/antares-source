package ch.scorpion.jabbah.io

/**
 * Utility methods for working with the [Storable] hierarchy defined by [Storable.getStorableChildren].
 */
object StorableHierarchy {

    /**
     * Collects all [Storable]s that are recursively reachable by [Storable.getStorableChildren].
     * @param storable the relative root [Storable]
     * @param consumer the object that collects the [Storable]s.
     */
    fun collect(storable: Storable, consumer: (Storable) -> Unit) {
        consumer.invoke(storable)
        for (child in storable.getStorableChildren()) {
            collect(child, consumer)
        }
    }

    /**
     * Finds the [Storable] with the specified storable ID.
     * @param storable the relative root [Storable] where the search is started.
     * @param storableId the storable ID to find.
     */
    fun find(storable: Storable, storableId: Int): Storable? {
        if (storable.storableId == storableId) {
            return storable
        }
        return find(storable.getStorableChildren(), storableId)
    }

    /**
     * Finds the [Storable] with the specified storable ID.
     * @param storables the [Storable]s where the search is started. Each iterated [Storable] is a root for a recursive search.
     * @param storableId the storable ID to find.
     */
    fun find(storables: Iterator<Storable>, storableId: Int): Storable? {
        for (storable in storables) {
            val result = find(storable, storableId)
            if (result != null) {
                return result
            }
        }
        return null
    }
}