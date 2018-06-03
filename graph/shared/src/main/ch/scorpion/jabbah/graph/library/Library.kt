package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.base.UUID


interface Library : LibraryDirectory {

    val libraryFolder: LibraryFolder

	/**
	 * The [LibraryService] to use when operation on this [Library]. Needed in order to be able to distinguish
	 * between different service implementations for libraries and projects.
	 */
	val libraryService: LibraryService

    /** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
    fun getMetaGraph(uuid: UUID): MetaGraph

    /** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
    fun getOptionalMetaGraph(uuid: UUID): MetaGraph?

    /** Checks whether a [MetaGraph] with [uuid] exists in this [Library]. */
    fun containsMetaGraph(uuid: UUID): Boolean

    /** Replaces the contents of this [Library] with the content of the specified [LibraryFolder].*/
    fun replaceContentsWith(libraryFolder: LibraryFolder)

    /**
     * Determines whether a [Graph] contains directly or recursively a [GraphElement]
     * with the specified UUID.
     */
    fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean

    /** Binds all [LibraryItem]s of this [Library] to this [Library] by calling [LibraryItem.bindTo]. */
    fun bindLibraryItems()

}
