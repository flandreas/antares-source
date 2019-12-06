package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryProperties

/**
 * A service for managing and persistently changing a [LibraryDictionary].
 *
 * Makes all changes immediately persistent using the [LibraryDictionaryPersistenceService]
 * provided during construction.
 */
class LibraryDictionaryService(
	private val persistenceService: LibraryDictionaryPersistenceService
) {

	private val dictionary: LibraryDictionary by lazy { persistenceService.load() }

	/** Determines whether the directory for storing the [LibraryDictionary] already exists.*/
	val directoryExists: Boolean get() = persistenceService.directoryExists

	fun getNames(): ImmutableList<String> {
		return dictionary.getLibraryNames()
	}

	fun existsName(name: String): Boolean = dictionary.existsName(name)

	fun contains(uuid: UUID): Boolean = dictionary.contains(uuid)

	fun getEntries(): ImmutableList<LibraryDictionaryEntry> = dictionary.getEntries()

	fun add(library: Library) {
		dictionary.add(library)
		store()
	}

	fun rename(library: Library, newName: String) {
		dictionary.rename(library, newName)
		store()
	}

	fun update(library: Library, properties: LibraryProperties) {
		dictionary.update(library, properties)
		store()
	}

	fun remove(uuid: UUID) {
		dictionary.remove(uuid)
		store()
	}

	private fun store() {
		persistenceService.store(dictionary)
	}
}