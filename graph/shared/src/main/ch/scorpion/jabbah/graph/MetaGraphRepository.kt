package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice

/**
 * A [MetaGraphRepository] is a repository of reusable [MetaGraph].
 *
 * A [MetaGraph] containing [SubGraphVertice]s will use a [MetaGraphRepository] to get access to the
 * [MetaGraph] that is referenced by the [SubGraphVertice].
 */
interface MetaGraphRepository {

	/** Returns the [ContainerLibraryElement] for the [MetaGraph] with the specified [uuid].*/
	fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement?

	/** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
	fun getMetaGraph(uuid: UUID): MetaGraph

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	fun getOptionalMetaGraph(uuid: UUID): MetaGraph?

	/** Checks whether a [MetaGraph] with [uuid] exists in this [MetaGraphRepository]. */
	fun containsMetaGraph(uuid: UUID): Boolean

	/**
	 * Returns the [Library] containing the [MetaGraph] with the specified [UUID].
	 */
	fun getContainingLibrary(uuid: UUID): Library?

	/**
	 * Determines whether a [Graph] contains directly or recursively a [GraphElement]
	 * with the specified UUID.
	 */
	fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean

	/**
	 * Creates a [MetaGraphBundle] for [metaGraph] containing [metaGraph] and all [MetaGraph]s directly
	 * or indirectly referenced by [metaGraph].
	 */
	fun createBundle(metaGraph: MetaGraph): MetaGraphBundle
}