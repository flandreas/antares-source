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
	 * Signals whether this [Storable] is currently in its [read] methods.
	 * Can be used by implementations to avoid unnecessary event broadcasting etc. while
	 * being deserialized from persistent store.
	 * */
	var isReading: Boolean

	/** The negation of [isReading]. */
	val isNotReading: Boolean get() = !isReading

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
	 * Reads this [Storable] from the specified [StoreReader] while flagging [isReading] during the
	 * entire read operation.
	 * Should only be called by [StoreReader] who initiates top-level reads. [Storables][Storable] that
	 * read sub-[Storables][Storable] should use the appropriate methods of [StoreReader], such as
	 * [StoreReader.readStorables].
	 */
	fun readFromStore(reader: StoreReader) {
		try {
			isReading = true
			read(reader)
		} finally {
			isReading = false
		}
	}
}

abstract class AbstractStorable : Storable {
	override var isReading: Boolean = false
}