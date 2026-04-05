package io.antarescircuit.jabbah.graph.library.dictionary

import io.antarescircuit.jabbah.app.CurrentWorkspaceEvent
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.resettableLazy
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryProperties

/**
 * A service for managing and persistently changing a [LibraryDictionary].
 *
 * Makes all changes immediately persistent using the [LibraryDictionaryPersistenceService]
 * provided during construction.
 */
class LibraryDictionaryService(
	private val persistenceService: LibraryDictionaryPersistenceService,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val dictionary = resettableLazy { persistenceService.load() }

	private val currentWorkspaceHandler: EventHandler<CurrentWorkspaceEvent> = {
		if (!it.isPrepare) {
			dictionary.reset()
		}
	}

	/** Determines whether the directory for storing the [LibraryDictionary] already exists.*/
	val directoryExists: Boolean get() = persistenceService.directoryExists

	val entriesCount: Int get() = dictionary.value.size

	init {
		eventBus.register(CurrentWorkspaceEvent::class, currentWorkspaceHandler)
	}

	fun dispose() {
		eventBus.unregister(currentWorkspaceHandler)
	}

	fun ensureLibraryDirectory() {
		persistenceService.ensureLibraryDirectory()
	}

	fun existsName(name: TranslatableText, except: UUID? = null): Boolean = dictionary.value.existsName(name, except)

	fun contains(uuid: UUID): Boolean = dictionary.value.contains(uuid)

	fun getEntries(): ImmutableList<LibraryDictionaryEntry> = dictionary.value.getEntries()

	fun getEntry(uuid: UUID): LibraryDictionaryEntry? = dictionary.value.getEntry(uuid)

	fun add(library: Library) {
		dictionary.value.add(library)
		store()
	}

	fun rename(library: Library, newName: TranslatableText) {
		dictionary.value.rename(library, newName)
		store()
	}

	fun update(library: Library, properties: LibraryProperties) {
		dictionary.value.update(library, properties)
		store()
	}

	fun remove(uuid: UUID) {
		dictionary.value.remove(uuid)
		store()
	}

	private fun store() {
		persistenceService.store(dictionary.value)
	}
}