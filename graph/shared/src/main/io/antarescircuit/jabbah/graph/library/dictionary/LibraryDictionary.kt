package io.antarescircuit.jabbah.graph.library.dictionary

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.collection.ImmutableList
import io.antarescircuit.jabbah.base.collection.toImmutableList
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.*
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryIdentification
import io.antarescircuit.jabbah.graph.library.LibraryProperties
import io.antarescircuit.jabbah.graph.library.LibraryVisibility
import io.antarescircuit.jabbah.io.*

/**
 * Maintains all [Libraries][Library] of an installation/user by mapping [Library] names to their [UUID],
 * avoiding the need to read all [Libraries][Library] from persistent store to do this mapping.
 */
class LibraryDictionary : AbstractStorable() {

	/** Maps a [UUID] of a [LibraryDictionaryEntry] to its [LibraryDictionaryEntry].*/
	private val entries = mutableMapOf<UUID, LibraryDictionaryEntry>()

	/** Returns the number of mappings in this [LibraryDictionary].*/
	val size: Int get() = entries.size

	/** Determines whether this [LibraryDictionary] contains a mapping with the given [UUID].*/
	fun contains(uuid: UUID): Boolean = entries.contains(uuid)

	/** Returns the [LibraryDictionaryEntry] with the specified [UUID], or `null` if it doesn't exist.*/
	fun getEntry(uuid: UUID): LibraryDictionaryEntry? = entries[uuid]

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

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		writer.writeStorables("entries", entries.values.iterator())
	}

	override fun read(reader: StoreReader) {
		reader
			.readStorables<LibraryDictionaryEntry>("entries")
			.forEach { entries[it.uuid] = it }
	}
}

/**
 * Represents an individual entry in [LibraryDictionary].
 * Contains information redundant to [Library] for faster access.
 */
class LibraryDictionaryEntry(
	var uuid: UUID = System.createUUID(),
	initialName: TranslatableText = TranslatableText(),
	var author: UserIdentity = UserIdentity.random(),
	var initialDescription: TranslatableText = TranslatableText(),
	var visibility: LibraryVisibility = LibraryVisibility.Private
) : AbstractStorable(), Namable, Describable {

	companion object {
		fun forLibrary(library: Library): LibraryDictionaryEntry =
			LibraryDictionaryEntry(
				uuid = library.uuid,
				initialName = library.name.translation,
				author = library.author,
				initialDescription = library.description.translation,
				visibility = library.visibility)
	}

	/** ---- [Any] */

	override fun toString(): String = name.translation.getTranslation()

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(initialName))

	override var description: Description by observableDescription(Description(initialDescription))

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		name.write("name", writer)
		writer.writeString("author", author.toString())
		description.write("desc", writer)
		writer.writeString("visibility", visibility.customName)
	}

	override fun read(reader: StoreReader) {
		uuid = System.createUUID(reader.readString("uuid"))
		name = Name.read("name", reader)
		description = Description.read("desc", reader)
		author = UserIdentity(reader.readString("author"))
		if (reader.hasAttribute("visibility")) {
			// Backward compatibility
			visibility = LibraryVisibility.withName(reader.readString("visibility"))
		}
	}

	/** ---- [LibraryDictionaryEntry] */

	val identification: LibraryIdentification get() = LibraryIdentification(uuid, author)

	fun updateFrom(properties: LibraryProperties) {
		name = Name(properties.name)
		description = Description(properties.description)
		visibility = properties.visibility
		properties.author?.let { author = it }
	}
}