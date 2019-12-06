package ch.scorpion.jabbah.graph.library.dictionary

interface LibraryDictionaryPersistenceService {

	/** Determines whether the directory for storing the [LibraryDictionary] already exists.*/
	val directoryExists: Boolean

	fun load(): LibraryDictionary

	fun store(dictionary: LibraryDictionary)
}

class UnimplementedLibraryDictionaryPersistenceService : LibraryDictionaryPersistenceService {

	override val directoryExists: Boolean = false

	override fun load(): LibraryDictionary {
		throw UnsupportedOperationException("not implemented")
	}

	override fun store(dictionary: LibraryDictionary) {
		throw UnsupportedOperationException("not implemented")
	}
}