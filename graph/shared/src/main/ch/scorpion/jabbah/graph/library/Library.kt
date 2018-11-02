package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository


interface Library : LibraryDirectory, MetaGraphRepository {

	/** The universal unique ID of this [Library]. Used for referencing this [Library] from projects.*/
	// val uuid: UUID

	/**
	 * The [UUID] of the [Library] imported by this [Library], i.e. the [Library] from which this [Library]
	 * imports [MetaGraph]s. This is currently only used by projects.
	 */
	// val importedLibrary: UUID?

	/** The UUID of the [ContainerLibraryElement] to be opened per default.*/
	var defaultElementUUID: UUID?

    val libraryFolder: LibraryFolder

	/**
	 * The [LibraryService] to use when operation on this [Library]. Needed in order to be able to distinguish
	 * between different service implementations for libraries and projects.
	 */
	val libraryService: LibraryService

	/** Returns the [ContainerLibraryElement] with the specified [UUID].*/
	fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement?

    /** Replaces the contents of this [Library] with the content of the specified [LibraryFolder].*/
    fun replaceContentsWith(libraryFolder: LibraryFolder)

    /** Binds all [LibraryItem]s of this [Library] to this [Library] by calling [LibraryItem.bindTo]. */
    fun bindLibraryItems()

	/**
	 * The [ContainerLibraryElement] to be opened by default (if required) when this [Library] is opened.
	 * The current implementation simply returns the first element (if existing).
	 */
	fun getDefaultElement(): ContainerLibraryElement?

}

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

	/** Adds the given mapping to this [LibraryDictionary] and makes the change persistent.*/
	fun add(name: String, uuid: UUID)

	/** Removes the mapping with the given [UUID] from this [LibraryDictionary] and makes the change persistent.*/
	fun remove(uuid: UUID)

	/** Returns the names of the stored [Libraries][Library].*/
	fun getLibraryNames(): ImmutableList<String>

	/** Returns the [UUID] of the [Library] with the specified name.*/
	fun getUUIDofName(name: String): UUID

	/** Returns the name of the [Library] with the specified [UUID].*/
	fun getNameOfUUID(uuid: UUID): String
}

/** Null pattern.*/
class UnimplementedLibraryDictionary : LibraryDictionary {

	override val size: Int get() = TODO("not implemented")

	override fun load() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun contains(uuid: UUID): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun add(name: String, uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun remove(uuid: UUID) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getLibraryNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getUUIDofName(name: String): UUID {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getNameOfUUID(uuid: UUID): String {
		throw UnsupportedOperationException("not implemented")
	}
}
