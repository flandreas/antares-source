package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.NamableImpl
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.io.*

/**
* Maintains all [Libraries][Library] of an installation/user by mapping [Library] names to their [UUID],
* avoiding the need to read all [Libraries][Library] from persistent store to do this mapping.
*/
class LibraryDictionary : Storable {

	/** Maps a [UUID] of a [LibraryDictionaryEntry] to its [LibraryDictionaryEntry].*/
	private val entries = mutableMapOf<UUID, LibraryDictionaryEntry>()

	/** Returns the number of mappings in this [LibraryDictionary].*/
	val size: Int get() = entries.size

	/** Determines whether this [LibraryDictionary] contains a mapping with the given [UUID].*/
	fun contains(uuid: UUID): Boolean = entries.contains(uuid)

	/** Returns the names of the stored [Libraries][Library].*/
	fun getLibraryNames(): ImmutableList<String> = entries.values.map { it.name.value }.sorted().toImmutableList()

	/** Returns all [LibraryDirectoryEntries][LibraryDictionaryEntry] of this [LibraryDictionary].*/
	fun getEntries(): ImmutableList<LibraryDictionaryEntry> = entries.values.toList().toImmutableList()

	/** Returns the [UUID] of the [Library] with the specified name.*/
	//fun getUUIDofName(name: String): UUID = getEntryByName(name).uuid

	/** Checks whether this [LibraryDictionary] contains a [Library] with the given name in any language. */
	fun existsName(name: TranslatableText, except: UUID?): Boolean =
		entries.values
			.filter { except == null || it.uuid != except }
			.any { it.name.translation.isAnyEqualOf(name) }

	/** Returns the name of the [Library] with the specified [UUID].*/
	/*
	fun getNameOfUUID(uuid: UUID): TranslatableText {
		val entry = entries[uuid]
		if (entry == null) {
			LOG.error("requesting name of non-existing library with UUID $uuid")
			throw IllegalArgumentException()
		}
		return entry.name.translation
	}
	*/

	/** Adds the specified [Library] to this [LibraryDictionary]*/
	fun add(library: Library) {
		if (!contains(library.uuid)) {
			entries[library.uuid] = LibraryDictionaryEntry.forLibrary(library)
		}
	}

	/** Removes the [Library] with the specified [UUID] from this [LibraryDictionary]. */
	fun remove(uuid: UUID) {
		entries.remove(uuid)
	}

	fun rename(library: Library, newName: TranslatableText) {
		if (entries[library.uuid]!!.name.translation != newName) {
			entries[library.uuid]!!.name.translation = newName
		}
	}

	fun update(library: Library, properties: LibraryProperties) {
		entries[library.uuid]!!.updateFrom(properties)
	}

	/*
	private fun getEntryByName(name: String): LibraryDictionaryEntry {
		val entry = entries.values.firstOrNull { it.name.value == name }
		if (entry == null) {
			LOG.error("requesting entry of non-existing library with name $name")
			throw IllegalArgumentException()
		}
		return entry
	}
	*/

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

}

/**
 * Represents an individual entry in [LibraryDictionary].
 * Contains information redundant to [Library] for faster access.
 */
class LibraryDictionaryEntry(
	var uuid: UUID = System.get().createUUID(),
	initialName: TranslatableText = TranslatableText(),
	var author: UUID = System.get().createUUID(),
	var initialDescription: TranslatableText = TranslatableText(),
	private val namable: NamableImpl = NamableImpl(initialName),
	private val describable: Describable = DescribableImpl(initialDescription)
) : Storable, Namable by namable, Describable by describable {

	companion object {
		fun forLibrary(library: Library): LibraryDictionaryEntry {
			return LibraryDictionaryEntry(
				uuid = library.uuid,
				initialName = library.name.translation,
				author = library.author,
				initialDescription = library.description.translation
			)
		}
	}

	/** ---- [Any] */

	override fun toString(): String = name.translation.getTranslation()

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		name.write("name", writer)
		writer.writeString("author", author.toString())
		description.write("desc", writer)
	}

	override fun read(reader: StoreReader) {
		uuid = System.get().createUUID(reader.readString("uuid"))
		name.read("name", reader)
		author = System.get().createUUID(reader.readString("author"))
		description.read("desd", reader)
	}

	/** ---- [LibraryDictionaryEntry] */

	fun updateFrom(properties: LibraryProperties) {
		description.translation = properties.description
	}
}