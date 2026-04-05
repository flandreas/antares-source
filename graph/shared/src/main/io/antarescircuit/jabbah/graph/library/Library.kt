package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.edit.auth.User
import io.antarescircuit.jabbah.edit.auth.UserIdentity
import io.antarescircuit.jabbah.edit.model.image.ImageRepository
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Describable
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.io.Storable

/**
 * A [Library] is a hierarchically structured [MetaGraphRepository] that has been composed by a particular user
 * (which might be the developer of the application that uses this software).
 *
 * Note that the name of the [Library] is maintained as the name of its root [LibraryDirectory].
 */
interface Library : MetaGraphRepository, ImageRepository, Storable, Namable, Describable, LibraryDirectory {

	/** Returns the [LibraryIdentification] used for building persistent paths to this [Library]. */
	val identification: LibraryIdentification get() = if (isSystem) {
		LibraryIdentification(uuid, null)
	} else {
		LibraryIdentification(uuid, author)
	}

	/** The universal unique ID of this [Library]. Used for referencing this [Library] from projects.*/
	var uuid: UUID

	/**
	 * Determines whether this [Library] is system build-in and can only be edited by the developer.
	 */
	var isSystem: Boolean

	/**
	 * The identity of the [User] that is the author (and therefore owner) of this [Library].
	 * A [Library] and its elements can only be edited by the author of the [Library].
	 */
	var author: UserIdentity

	/** The [UUIDs][UUID] of the [Libraries][Library] imported by this [Library].*/
	val importedLibraryIds: Set<UUID>

	/**
	 * A lazily initialized collection of the transitively expanded imported [Libraries][Library]
	 * according to [importedLibraryIds], including this [Library].
	 */
	val expandedImports: LibraryImports

	/** The description of the contents of this [Library], i.e. its inner [LibraryItem]s. */
	val directory: LibraryDirectory

	/** The UUID of the [ContainerLibraryElement] to be opened per default.*/
	var defaultElementUUID: UUID?

	/** Determines who can see [Graphs][Graph] of this [Library]. Relates to "publishing" [Libraries][Library].*/
	var visibility: LibraryVisibility

	/**
	 * Convenience accessors for user-editable properties of this [Library].
	 * Setting these properties does NOT make the [Library] changes persistent, nor does this class
	 * make any validations, such as to ensure that [Library] names are globally unique. This is the responsibility
	 * of a domain service class.
	 */
	var properties: LibraryProperties

	/**
	 * Contains preferences that influence execution of [Graph]s in a [Library] and are therefore
	 * copied from the local preferences to go along with exported [Libraries][Library].
	 */
	val preferences: LibraryPreferences

	/**
	 * The [LibraryService] to use when operation on this [Library]. Needed in order to be able to distinguish
	 * between different service implementations for libraries and projects.
	 */
	val libraryService: LibraryService

	/** Returns the number of [MetaGraph] contained in this [Library]. */
	val metaGraphCount: Int

	/** Returns the [UUID] of a [MetaGraphs][MetaGraph] directly contained in this [Library]. */
	val metaGraphIds: List<UUID>

	/** Volatile property to mark an imported [Library] not found on the current system.*/
	var isBrokenImport: Boolean

    /** Binds all [LibraryItem]s of this [Library] to this [Library] by calling [LibraryItem.bindTo]. */
    fun bindLibraryItems()

	/**
	 * The [ContainerLibraryElement] to be opened by default (if required) when this [Library] is opened.
	 * The current implementation simply returns the first element (if existing).
	 */
	fun getDefaultElement(): ContainerLibraryElement?

	/**
	 * Checks whether a [MetaGraph] with [uuid] exists locally in this [Library].
	 * This is in contrast to [MetaGraphRepository.containsMetaGraph], which also checks imported [Library Libraries].
	 */
	fun containsMetaGraphLocally(uuid: UUID): Boolean

	/**
	 * Determines whether this [Library] contains [ContainerLibraryElement] for all [Graph]s
	 * recursively referenced in the specified [Graph].
	 */
	fun containsAllRecursivelyReferencedBy(graph: Graph): Boolean

	/** Creates an appropriate [Savable] for the specified [ContainerLibraryElement].*/
	fun createSavable(element: ContainerLibraryElement): Savable

	/** Adds the [Library] with the specified [UUID] to the ones imported by this [Library]. */
	fun addImport(libraryId: UUID)

	/**
	 * Removes the [Library] with the specified [UUID] from the ones imported by this [Library].
	 * @param libraryId the [UUID] of the [Library] to remove from [LibraryImports]
	 * @param replacingSystemLibraries the [UUIDs][UUID] of the system [Libraries][Library]
	 * to be imported instead to resolve dangling references
	 */
	fun removeImport(libraryId: UUID, replacingSystemLibraries: Set<UUID>)

	/**
	 * Returns the first locally in this [Library] contained [LibraryItem] that
	 * fulfills the specified filter. "Locally" means that [expandedImports] is not involved.
	 */
	fun firstLocalItemOrNull(filter: (LibraryItem) -> Boolean): LibraryItem?

	/**
	 * Returns all locally in this [Library] contained [LibraryItem] that
	 * fulfills the specified filter. "Locally" means that [expandedImports] is not involved.
	 */
	fun allLocalItems(filter: (LibraryItem) -> Boolean): List<LibraryItem>
}

/**
 * Used by persistence-oriented classes that store [Libraries][Library] per owner.
 * The owner is empty for system-level [Libraries][Library] that can't be changed by the users
 * and are stored centrally.
 */
data class LibraryIdentification(
	val uuid: UUID,
	val owner: UserIdentity?
)

/**
 * Represents those properties of a [Library] that can be provided by the user when creating a [Library],
 * or that can be updated by the user on an existing [Library].
 */
data class LibraryProperties(
	val name: TranslatableText = TranslatableText(),
	val description: TranslatableText = TranslatableText(),
	val visibility: LibraryVisibility = LibraryVisibility.Hidden,
	val author: UserIdentity? = null,
	val importUuid: UUID? = null
) {
	companion object {
		fun ofLibrary(library: Library): LibraryProperties =
			LibraryProperties(
				library.name.translation,
				library.description.translation,
				library.visibility,
				library.author)
	}
}

