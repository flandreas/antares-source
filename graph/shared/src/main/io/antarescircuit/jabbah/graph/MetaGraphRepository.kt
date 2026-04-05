package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVertice

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

	/**
	 * Establishes [wrapper] such that it is asked first when retrieving [MetaGraphs][MetaGraph].
	 * If [wrapper] doesn't contain the requested [MetaGraph], this [MetaGraphRepository] is then asked.
	 * Needed instead of a regular wrapper because objects like [SubGraphVertice] are constructed with
	 * fixed references to a global [MetaGraphRepository], such as that of [LibraryModule.libraryHolder].
	 */
	fun wrapWith(wrapper: MetaGraphRepository)

	/** Only used by wrappers which can't call [getMetaGraph] without infinite recursion. */
	fun getMetaGraphUnwrapped(uuid: UUID): MetaGraph

	/** Removes the wrapper previously installed by [wrapWith]. */
	fun unwrap()
}