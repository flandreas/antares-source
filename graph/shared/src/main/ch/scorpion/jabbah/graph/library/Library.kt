package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.user.User
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph

/**
 * A [Library] is a hierarchically structured [MetaGraphRepository] that has been composed by a particular user
 * (which might be the developer of the application that uses this software).
 *
 * Note that the name of the [Library] is maintained as the name of its root [LibraryDirectory].
 */
interface Library : LibraryDirectory, MetaGraphRepository {

	/** The universal unique ID of this [Library]. Used for referencing this [Library] from projects.*/
	var uuid: UUID

	/** Contains a short description describing the purpose of this [Library].*/
	var description: String?

	/**
	 * The [UUID] of the [User] that is the author (and therefore owner) of this [Library].
	 * A [Library] and its elements can only be edited by the author of the [Library].
	 */
	var author: UUID

	/** The UUID of the [ContainerLibraryElement] to be opened per default.*/
	var defaultElementUUID: UUID?

	/**
	 * Convenience accessors for user-editable properties of this [Library].
	 * Setting these properties does NOT make the [Library] changes persistent, nor does this class
	 * make any validations, such as to ensure that [Library] names are globally unique. This is the responsibility
	 * of a domain service class.
	 */
	var properties: LibraryProperties

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

	/**
	 * Determines whether this [Library] contains [ContainerLibraryElement] for all [Graph]s
	 * recursively referenced in the specified [Graph].
	 */
	fun containsAllRecursivelyReferencedBy(graph: Graph): Boolean

}

/**
 * Represents those properties of a [Library] that can be provided by the user when creating a [Library],
 * or that can be updated by the user on an existing [Library].
 */
data class LibraryProperties(val name: String, val description: String? = null)

/**
 * Posted by domain services on [EventBus] when the entire [LibraryProperties] have been changed.
 * Note that this event is NOT posted by [Library] itself.
 */
data class LibraryPropertiesEvent(val library: Library, val properties: LibraryProperties)
