package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileSystems

/**
 * An implementation of [LibraryDictionary] that stores the dictionary in the local file system.
 */
class FileLibraryDictionary(
	private val directoryPath: String = "",
	private val dictionaryFileName: String = "library.dictionary",
	private val eventBus: EventBus = BaseModule.eventBus
) : LibraryDictionary, Storable {

	companion object {
		private val LOG by logger(FileLibraryDictionary::class)
	}
	
	/** Maps a [UUID] of a [FileLibraryDictionaryEntry] to its [FileLibraryDictionaryEntry].*/
	private val entries = mutableMapOf<UUID,FileLibraryDictionaryEntry>()

	private val libraryCreatedHandler: EventHandler<LibraryCreatedEvent> = {
		add(it.library.name, it.library.uuid)
	}

	init {
		eventBus.register(LibraryCreatedEvent::class, libraryCreatedHandler)
	}

	private fun dispose() {
		eventBus.unregister(LibraryCreatedEvent::class, libraryCreatedHandler)
	}

	/** ---- [LibraryDictionary] interface */

	override val size: Int get() = entries.size
	
	override fun add(name: String, uuid: UUID) {
		entries[uuid] = FileLibraryDictionaryEntry(uuid, name)
		store()
	}

	override fun remove(uuid: UUID) {
		entries.remove(uuid)
		store()
	}

	override fun contains(uuid: UUID): Boolean =
		entries.contains(uuid)
	
	override fun getLibraryNames(): ImmutableList<String> =
		entries.values.map { it.name }.sorted().toImmutableList()

	override fun getUUIDofName(name: String): UUID = getEntryByName(name).uuid

	override fun getNameOfUUID(uuid: UUID): String {
		val entry = entries[uuid]
		if (entry == null) {
			LOG.error("FileLibraryDictionary: requesting name of non-existing library with UUID $uuid")
			throw IllegalArgumentException()
		}
		return entry.name
	}

	override fun load() {
		val path = buildLibraryDirectoryFilePath()
		try {
			FileInputStream(path).use {
				try {
					LOG.debug("FileLibraryDictionary: loading entries")
					val storeReader = StoreXmlReader(ElectricXmlReader(it))
					val dictionary = storeReader.readStorable() as FileLibraryDictionary
					this.entries.clear()
					this.entries.putAll(dictionary.entries)
					dictionary.dispose()
				} catch (e: Throwable) {
					LOG.error("FileLibraryDictionary: error while loading dictionary file $path: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("FileLibraryDictionary: error while accessing dictionary file $path: ${e.message}")
		}
	}

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		writer.writeStorables("entries", getStorableChildren())
	}

	override fun read(reader: StoreReader) {
		reader
			.readStorables("entries")
			.map { it as FileLibraryDictionaryEntry }
			.forEach { entries[it.uuid] = it }
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return entries.values.iterator()
	}

	/** --- [FileLibraryDictionary] */

	private fun getEntryByName(name: String): FileLibraryDictionaryEntry {
		val entry = entries.values.firstOrNull { it.name == name }
		if (entry == null) {
			LOG.error("FileLibraryDictionary: requesting entry of non-existing library with name $name")
			throw IllegalArgumentException()
		}
		return entry
	}

	private fun store() {
		val path = buildLibraryDirectoryFilePath()
		try {
			FileOutputStream(path).use {
				try {
					LOG.debug("FileLibraryDictionary: storing entries")
					val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
					storeWriter.writeStorable(this)
				} catch (e: Throwable) {
					LOG.error("FileLibraryDictionary: error while storing dictionary file $path: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("FileLibraryDictionary: error while accessing dictionary file $path: ${e.message}")
		}
	}

	private fun buildLibraryDirectoryFilePath(): String {
		return FileSystems.getDefault().getPath(directoryPath, dictionaryFileName).toString()
	}
}

data class FileLibraryDictionaryEntry(
	var uuid: UUID = System.get().createUUID(),
	var name: String = ""
) : Storable {

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
	
	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		writer.writeString("name", name)
	}

	override fun read(reader: StoreReader) {
		uuid = System.get().createUUID(reader.readString("uuid"))
		name = reader.readString("name")
	}
}
