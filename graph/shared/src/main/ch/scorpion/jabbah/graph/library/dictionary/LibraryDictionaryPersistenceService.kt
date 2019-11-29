package ch.scorpion.jabbah.graph.library.dictionary

interface LibraryDictionaryPersistenceService {

	fun load(): LibraryDictionary

	fun store(dictionary: LibraryDictionary)
}

class UnimplementedLibraryDictionaryPersistenceService : LibraryDictionaryPersistenceService {

	override fun load(): LibraryDictionary {
		throw UnsupportedOperationException("not implemented")
	}

	override fun store(dictionary: LibraryDictionary) {
		throw UnsupportedOperationException("not implemented")
	}
}