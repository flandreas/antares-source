package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraphRepository


interface Library : LibraryDirectory, MetaGraphRepository {

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

    /**
     * Determines whether a [Graph] contains directly or recursively a [GraphElement]
     * with the specified UUID.
     */
    fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean

    /** Binds all [LibraryItem]s of this [Library] to this [Library] by calling [LibraryItem.bindTo]. */
    fun bindLibraryItems()

	/**
	 * The [ContainerLibraryElement] to be opened by default (if required) when this [Library] is opened.
	 * The current implementation simply returns the first element (if existing).
	 */
	fun getDefaultElement(): ContainerLibraryElement?

	/** ---- [MetaGraphRepository] */

	/** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
	override fun getMetaGraph(uuid: UUID): MetaGraph

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph?

	/** Checks whether a [MetaGraph] with [uuid] exists in this [Library]. */
	override fun containsMetaGraph(uuid: UUID): Boolean

}
