package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Posted on [EventBus] when a [Library] is to be opened and is to replace the currently open [Library].
 * Subscribers for this event can raise their veto, such as the application class that keeps track of
 * changes of the current [Library]'s or current project's open [MetaGraph].
 */
data class OpenLibraryRequest(val library: Library)

/**
 * Posted on [EventBus] when a new [Library] has been created
 * @property library the created [Library]
 */
data class LibraryCreatedEvent(val library: Library)

/**
 * Provides methods for managing multiple [Libraries][Library].
 */
interface LibraryManagementService {

	/** Returns the name of the default [Library] which is one of those provided by the system, not by the user.*/
	val defaultLibraryName: String

	/** Returns the names of the stored [Libraries][Library].*/
	fun getLibraryNames(): ImmutableList<String>

	/** Returns all [LibraryDictionaryEntries][LibraryDictionaryEntry].*/
	fun getLibraryDirectoryEntries(): ImmutableList<LibraryDictionaryEntry>

	/** Determines whether [name] already exists as the name of a stored [Library].*/
	fun exists(name: String): Boolean

	/** Loads the [Library] with the specified name from persistent store.*/
	fun loadLibrary(name: String): Library

	/**
	 * Creates a new default [Library] with the given name and stores it in persistent store.
	 * Posts a [LibraryCreatedEvent] on [EventBus].
	 * @param properties the initial properties of the new [Library]
	 * @param templateLibraryName the name of the [Libary] to be copied as a template.
	 *      If `null`, an empty [Library] is created.
	 * @return the created [Library]
	 * @throws IllegalArgumentException if a [Library] with name [name] already exists
	 */
	fun create(properties: LibraryProperties, templateLibraryName: String?): Library

	/**
	 * Updates the currently open [Library] with the specified properties and stores it in persistent store.
	 * @throws IllegalArgumentException if [properties] are not consistent, e.g. if a [Library]
	 * with the specified name already exists.
	 * @throws IllegalStateException if no [Library] is currently open
	 * Posts [LibraryPropertiesEvent] on this [LibraryManagementService]'s [EventBus].
	 */
	fun update(properties: LibraryProperties)

	/**
	 * Loads and opens the [Library] with the specified name, while closing a currently open project.
	 * @throws IllegalArgumentException if a [Library] with name [name] doesn't exist
	 */
	fun open(name: String): Library

	/**
	 * Loads and opens the [Library] with the specified [UUID], while closing a currently open project.
	 * @throws IllegalArgumentException if a [Library] with [UUID] [uuid] doesn't exist
	 */
	fun open(uuid: UUID): Library

	/** Opens the specified [Library], while closing a currently open project*/
	fun open(library: Library)

	/**
	 * Deletes the [Library] with the specified name.
	 * @throws IllegalArgumentException if the [Library] is currently open
	 */
	fun delete(name: String)

	fun canCopyContainerLibraryElement(element: ContainerLibraryElement, destination: Library): Boolean

	/**
	 * Copies the specified [LibraryElement] from its [Library] to the destination [LibraryDirectory],
	 * which can also be part of another [Library].
	 */
	fun copyLibraryElement(element: LibraryElement, destination: LibraryDirectory)
}

/** Null pattern.*/
class UnimplementedLibraryManagementService : LibraryManagementService {

	override val defaultLibraryName: String get() = TODO("not implemented")

	override fun getLibraryNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getLibraryDirectoryEntries(): ImmutableList<LibraryDictionaryEntry> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exists(name: String): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun loadLibrary(name: String): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun create(properties: LibraryProperties, templateLibraryName: String?): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun update(properties: LibraryProperties) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(name: String): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(uuid: UUID): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun open(library: Library) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun delete(name: String) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun canCopyContainerLibraryElement(element: ContainerLibraryElement, destination: Library): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun copyLibraryElement(element: LibraryElement, destination: LibraryDirectory) {
		throw UnsupportedOperationException("not implemented")
	}
}