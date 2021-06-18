package ch.scorpion.jabbah.io

/**
 * A [Storable] is an object that can be written to and read from persistent store.
 */
interface Storable {

	/**
	 * Determines whether a global ID is generated and written when this [Storable] gets stored.
	 * [Storable]s than can persistently be referenced by other [Storable] must return `true`.
	 */
	val isReferencable: Boolean get() = true

    /**
     * Asks this [Storable] to resolve a [Reference] to another [Storable] that it has requested to be
     * resolved during [read] using [ReferenceResolver.requestResolution].
     */
    fun resolve(reference: Reference, referenceResolver: ReferenceResolver)

    /**
     * This method is called by a [ReferenceResolver] after all resolutions have been done. A [Storable] implementation
     * can overwrite this method in order to implement logic that depends on the existence of the entire object tree.
     */
    fun resolutionDone() {
        // empty
    }

	/**
	 * This method is called by a [ReferenceResolver] after all [Storable.resolutionDone]s have been called.
	 * A [Storable] implementation can overwrite this method in order to implement logic that depends on completed
	 * resolution of the entire object tree.
	 */
	fun allResolutionDone() {
		// empty
	}

    /** Writes the properties of this [Storable] to persistent store using the specified [StoreWriter].*/
    fun write(writer: StoreWriter)

    /** Reads the properties of this [Storable] from persistent store using the specified [StoreReader].*/
    fun read(reader: StoreReader)
}