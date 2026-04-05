package io.antarescircuit.jabbah.graph.library.dictionary

interface LibraryDictionaryPersistenceService {

	/** Determines whether the directory for storing the [LibraryDictionary] already exists.*/
	val directoryExists: Boolean

	/** Creates the directory for storing the [LibraryDictionary] if it doesn't exist already*/
	fun ensureLibraryDirectory()

	fun load(): LibraryDictionary

	fun store(dictionary: LibraryDictionary)
}

class UnimplementedLibraryDictionaryPersistenceService : LibraryDictionaryPersistenceService {

	override val directoryExists: Boolean = false

	override fun ensureLibraryDirectory() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun load(): LibraryDictionary {
		throw UnsupportedOperationException("not implemented")
	}

	override fun store(dictionary: LibraryDictionary) {
		throw UnsupportedOperationException("not implemented")
	}
}