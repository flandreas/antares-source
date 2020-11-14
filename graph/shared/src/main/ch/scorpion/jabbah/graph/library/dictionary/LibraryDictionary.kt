package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.*
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

	/** Checks whether this [LibraryDictionary] contains a [Library] with the given name in any language. */
	fun existsName(name: TranslatableText, except: UUID?): Boolean =
		entries.values
			.filter { except == null || it.uuid != except }
			.any { it.name.translation.isAnyEqualOf(name) }

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
			entries[library.uuid]!!.name = Name(newName)
		}
	}

	fun update(library: Library, properties: LibraryProperties) {
		entries[library.uuid]!!.updateFrom(properties)
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

}

/**
 * Represents an individual entry in [LibraryDictionary].
 * Contains information redundant to [Library] for faster access.
 */
class LibraryDictionaryEntry(
	var uuid: UUID = System.createUUID(),
	initialName: TranslatableText = TranslatableText(),
	var author: UUID = System.createUUID(),
	var initialDescription: TranslatableText = TranslatableText()
) : Storable, Namable, Describable {

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

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(initialName))

	override var description: Description by observableDescription(Description(initialDescription))

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		name.write("name", writer)
		writer.writeString("author", author.toString())
		description.write("desc", writer)
	}

	override fun read(reader: StoreReader) {
		uuid = System.createUUID(reader.readString("uuid"))
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		author = System.createUUID(reader.readString("author"))
	}

	/** ---- [LibraryDictionaryEntry] */

	fun updateFrom(properties: LibraryProperties) {
		description = Description(properties.description)
	}
}