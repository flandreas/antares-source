package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.Library
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

	val entriesCount: Int get() = dictionary.size

	fun existsName(name: TranslatableText, except: UUID? = null): Boolean = dictionary.existsName(name, except)

	fun contains(uuid: UUID): Boolean = dictionary.contains(uuid)

	fun getEntries(): ImmutableList<LibraryDictionaryEntry> = dictionary.getEntries()

	fun getEntry(uuid: UUID): LibraryDictionaryEntry? = dictionary.getEntry(uuid)

	fun add(library: Library) {
		dictionary.add(library)
		store()
	}

	fun rename(library: Library, newName: TranslatableText) {
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