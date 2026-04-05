package io.antarescircuit.jabbah.io

/**
 * Manages references to [Storable]s while they are read from a [StoreReader], and helps to resolve
 * persistent references into object references after all [Storable]s of a persistent context have been
 * instantiated.
 */
interface ReferenceResolver {

    /**
     * Adds a deserialized [Storable] to this [ReferenceResolver] for later establishing heap references to
     * it.
     * @param globalId the global, persistent ID of `storable`.
     * @param storable the [Storable] to be added.
     */
    fun addStorable(globalId: Int, storable: Storable)

    /**
     * Returns the [Storable] with the specified global, persistent ID in order to establish a heap reference to
     * it.
     * @param globalId the ID under which the Storable has previously been registered by [.addStorable].
     * @return the [Storable] with global ID `globalId`, or `null` if not found.
     */
    fun <T: Storable> getStorable(globalId: Int): T?

	/**
	 * Returns the global ID of the specified [Storable]. Only available if the [Storable] has already been
	 * deserialized and added using [addStorable], or if provided by implementations of this interface that have
	 * otherwise access to such [Storable]s.
	 * @throws IllegalArgumentException if an ID for [storable] is not available
	 */
	fun getGlobalId(storable: Storable): Int

    /**
     * Registers a request of a [Storable] to call him with [Storable.resolve]
     * after all [Storable]s have been instantiated.

     * @param requester the [Storable] that wishes to be called later.
     * @param reference information read by `requester` from persistent storage and expected by him to be passed
     * *        along when later being called with [Storable.resolve]
     */
    fun requestResolution(requester: Storable, reference: Reference)

    /**
     * Calls [Storable.resolve] on all [Storable]s that have been registered
     * by [.addStorable].
     */
    fun resolveReferences()

    fun resolveReferences(referenceResolver: ReferenceResolver)

}