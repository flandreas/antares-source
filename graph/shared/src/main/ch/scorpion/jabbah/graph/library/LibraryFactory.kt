package ch.scorpion.jabbah.graph.library

/**
 * A factory for creating new [Libraries][Library].
 */
interface LibraryFactory {

	/** Creates a new empty [Library] with the specified properties.*/
	fun createEmptyLibrary(properties: LibraryProperties): Library

	/**
	 * Creates a new [Library] that contains the [BaseLibraryElement]s of a particular application.
	 * This can be used as a starting point when creating new user provided [Libraries][Library].
	 */
	fun createBaseLibrary(properties: LibraryProperties): Library
}

/** Null pattern.*/
class UnimplementedLibraryFactory : LibraryFactory {

	override fun createEmptyLibrary(properties: LibraryProperties): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun createBaseLibrary(properties: LibraryProperties): Library {
		throw UnsupportedOperationException("not implemented")
	}
}