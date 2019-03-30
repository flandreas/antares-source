package ch.scorpion.jabbah.io

/**
 * A [Storable] is an object that can be written to and read from persistent store.
 */
interface Storable {

    /**
     * Holds the global identification of this [Storable] after it has been read from persistent store.
     * This is used to read composite [Storable]s that reference persistent object which have been read earlier,
     * like when cloning view objects from the same model object.
     *
     * The value of this attribute is typically set by a [ReferenceResolver] in its collection phase, before
     * the first [Storable] is written. [Storable]s that are not returned by any [getStorableChildren] method
     * are are therefore not affected by a [ReferenceResolver] collection phase should set this attribute to -1.
     */
    var storableId: Int

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

    /**
     * Returns the children of this [Storable]s that are also persistent. Used for traversing the entire
     * [Storable] tree in order to generate identifications.
     *
     * Implementing classes can use the following pattern for combining superclass and local [Storable]s:
     * `Iterators.concat(super.getStorableChildren(), listOf(localStorable).iterator())`
     */
    fun getStorableChildren(): Iterator<Storable>
}