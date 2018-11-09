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

interface LibraryManagementService {

	/** Returns the name of the default [Library] which is one of those provided by the system, not by the user.*/
	val defaultLibraryName: String

	/** Returns the currently open [Library].*/
	val currentLibrary: Library

	/** Returns the names of the stored [Libraries][Library].*/
	fun getLibraryNames(): ImmutableList<String>

	/** Determines whether [name] already exists as the name of a stored [Library].*/
	fun exists(name: String): Boolean

	/** Loads the [Library] with the specified name from persistent store.*/
	fun loadLibrary(name: String): Library

	/**
	 * Creates a new default [Library] with the given name and stores it in persistent store.
	 * Posts a [LibraryCreatedEvent] on [EventBus].
	 * @return the created [Library]
	 * @throws IllegalArgumentException if a [Library] with name [name] already exists
	 */
	fun create(name: String): Library

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
	 * If the deleted [Library] is currently open, the default [Library] is opened instead.
	 * @throws IllegalArgumentException if a [Library] with name [name] doesn't exist
	 */
	fun delete(name: String)
}

/** Null pattern.*/
class UnimplementedLibraryManagementService : LibraryManagementService {

	override val defaultLibraryName: String get() = TODO("not implemented")
	override val currentLibrary: Library get() = TODO("not implemented")

	override fun getLibraryNames(): ImmutableList<String> {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exists(name: String): Boolean {
		throw UnsupportedOperationException("not implemented")
	}

	override fun loadLibrary(name: String): Library {
		throw UnsupportedOperationException("not implemented")
	}

	override fun create(name: String): Library {
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
}