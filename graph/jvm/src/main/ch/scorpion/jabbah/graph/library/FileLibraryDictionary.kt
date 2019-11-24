package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileSystems

/**
 * An implementation of [LibraryDictionary] that stores the dictionary in the local file system.
 */
class FileLibraryDictionary(
	private val directoryPath: String = "",
	private val dictionaryFileName: String = "library.dictionary"
) : LibraryDictionary, Storable {

	companion object {
		private val LOG by logger(FileLibraryDictionary::class)
	}
	
	/** Maps a [UUID] of a [LibraryDictionaryEntry] to its [LibraryDictionaryEntry].*/
	private val entries = mutableMapOf<UUID,LibraryDictionaryEntry>()

	/** ---- [LibraryDictionary] interface */

	override val size: Int get() = entries.size
	
	override fun contains(uuid: UUID): Boolean =
		entries.contains(uuid)
	
	override fun getLibraryNames(): ImmutableList<String> =
		entries.values.map { it.name }.sorted().toImmutableList()

	override fun getEntries(): ImmutableList<LibraryDictionaryEntry> {
		return entries.values.toList().toImmutableList()
	}

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
				} catch (e: Throwable) {
					LOG.error("FileLibraryDictionary: error while loading dictionary file $path: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("FileLibraryDictionary: error while accessing dictionary file $path: ${e.message}")
		}
	}

	override fun add(library: Library) {
		if (!contains(library.uuid)) {
			entries[library.uuid] = LibraryDictionaryEntry.forLibrary(library)
			store()
		}
	}

	override fun remove(name: String) {
		entries.remove(getUUIDofName(name))
		store()
	}

	override fun rename(library: Library, newName: String) {
		if (entries[library.uuid]!!.name != newName) {
			entries[library.uuid]!!.name = newName
			store()
		}
	}

	override fun update(library: Library, properties: LibraryProperties) {
		entries[library.uuid]!!.updateFrom(properties)
		store()
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
			.readStorables<LibraryDictionaryEntry>("entries")
			.forEach { entries[it.uuid] = it }
	}

	override fun getStorableChildren(): Iterator<Storable> {
		return entries.values.iterator()
	}

	/** --- [FileLibraryDictionary] */

	private fun getEntryByName(name: String): LibraryDictionaryEntry {
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
