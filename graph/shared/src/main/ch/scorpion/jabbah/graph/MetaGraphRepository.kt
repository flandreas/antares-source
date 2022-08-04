package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule.libraryHolder
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.repository.SubGraphVerticeLocator
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

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
}

/** Combines the current [Library] and the current [Project] (if any) to a single [MetaGraphRepository].*/
class CombinedMetaGraphRepository(
	private val storableCreator: StorableCreator = IOModule.storableCreator
) : MetaGraphRepository {

	/** ---- [MetaGraphRepository] */

	override fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement? =
		libraryHolder.library.getContainerLibraryElement(uuid)

	/** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
	override fun getMetaGraph(uuid: UUID): MetaGraph =
		libraryHolder.library.getMetaGraph(uuid)

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? =
		libraryHolder.library.getOptionalMetaGraph(uuid)

	/** Checks whether a [MetaGraph] with [uuid] exists in this [MetaGraphRepository]. */
	override fun containsMetaGraph(uuid: UUID): Boolean =
		libraryHolder.library.containsMetaGraph(uuid)

	override fun getContainingLibrary(uuid: UUID): Library? =
		libraryHolder.library.getContainingLibrary(uuid)

	/** ---- [CombinedMetaGraphRepository] */

	/**
	 * Determines whether a [Graph] contains directly or recursively a [GraphElement]
	 * with the specified UUID.
	 */
	fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean {
		val metaGraph = getMetaGraph(graphUUID)
		if (metaGraph.graph.model!!.uuid == graphElementUUID) {
			return true
		}
		return SubGraphVerticeLocator(
			graph = metaGraph.graph.model!!,
			repository = this,
			storableCreator = storableCreator
		).contains(graphElementUUID)
	}

	/**
	 * Creates a [MetaGraphBundle] for [metaGraph] containing [metaGraph] and all [MetaGraph]s directly
	 * or indirectly referenced by [metaGraph].
	 */
	fun createBundle(metaGraph: MetaGraph): MetaGraphBundle {
		val systemLibReferences = mutableSetOf<UUID>()
		return MetaGraphBundle()
			.add(metaGraph)
			.also { bundle ->
				ContainerLibraryElementCollector(this)
					.collect(metaGraph.graph.graphView.graph!!)
					.forEach { metaGraphId ->
						val sourceSystemLib = getOptionalSystemLibraryId(metaGraphId)
						if (sourceSystemLib != null) {
							systemLibReferences.add(sourceSystemLib)
						} else {
							bundle.add(getMetaGraph(metaGraphId))
						}
					}
				bundle.referencedSystemLibraryIds.addAll(systemLibReferences)
			}
	}

	private fun getOptionalSystemLibraryId(metaGraphId: UUID): UUID? {
		val elem = getContainerLibraryElement(metaGraphId)
		if (elem != null && elem.library?.isSystem == true) {
			return elem.library!!.uuid
		}
		return null
	}
}