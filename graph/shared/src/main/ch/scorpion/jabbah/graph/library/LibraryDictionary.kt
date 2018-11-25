package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.io.*

/**
 * Maintains all [Libraries][Library] of an installation/user by mapping [Library] names to their [UUID],
 * avoiding the need to read all [Libraries][Library] from persistent store to do this mapping.
 */
interface LibraryDictionary {

	/** Returns the number of mappings in this [LibraryDictionary].*/
	val size: Int

	/** Loads the contents of this [LibraryDictionary] from persistent storage.*/
	fun load()

	/** Determines whether this [LibraryDictionary] contains a mapping with the given [UUID].*/
	fun contains(uuid: UUID): Boolean

	/** Returns the names of the stored [Libraries][Library].*/
	fun getLibraryNames(): ImmutableList<String>

	/** Returns all [LibraryDirectoryEntries][LibraryDictionaryEntry] of this [LibraryDictionary].*/
	fun getEntries(): ImmutableList<LibraryDictionaryEntry>

	/** Returns the [UUID] of the [Library] with the specified name.*/
	fun getUUIDofName(name: String): UUID

	/** Returns the name of the [Library] with the specified [UUID].*/
	fun getNameOfUUID(uuid: UUID): String
}

/** Null pattern.*/
class UnimplementedLibraryDictionary : LibraryDictionary {

	override val size: Int get() = 0

	override fun load() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun contains(uuid: UUID): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getLibraryNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getEntries(): ImmutableList<LibraryDictionaryEntry> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getUUIDofName(name: String): UUID {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getNameOfUUID(uuid: UUID): String {
		throw UnsupportedOperationException("not implemented")
	}
}

/**
 * Represents an individual entry in [LibraryDirectory].
 * Contains information redundant to [Library] for faster access.
 */
data class LibraryDictionaryEntry(
	var uuid: UUID = System.get().createUUID(),
	var name: String = "",
	var author: UUID = System.get().createUUID(),
	var desription: String? = null
) : Storable {

	companion object {
		fun forLibrary(library: Library): LibraryDictionaryEntry {
			return LibraryDictionaryEntry(
				uuid = library.uuid,
				name = library.name,
				author = library.author,
				desription = library.description
			)
		}
	}

	/** ---- [Any] */

	override fun toString(): String = name

	/** ---- [Storable] interface */

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		writer.writeString("name", name)
		writer.writeString("author", author.toString())
		writer.writeOptionalString("desc", desription)
	}

	override fun read(reader: StoreReader) {
		uuid = System.get().createUUID(reader.readString("uuid"))
		name = reader.readString("name")
		author = System.get().createUUID(reader.readString("author"))
		desription = reader.readOptionalString("desc")
	}

	/** ---- [LibraryDictionaryEntry] */

	fun updateFrom(properties: LibraryProperties) {
		desription = properties.description
	}
}