package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
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

/** Combines the current [Library] and the current [Project] (if any) to a single [MetaGraphRepository].*/
class CombinedMetaGraphRepository(
	private val storableCreator: StorableCreator = IOModule.storableCreator
) : MetaGraphRepository {

	override fun getContainerLibraryElement(uuid: UUID): ContainerLibraryElement? {
		return LibraryModule.libraryHolder.library.getContainerLibraryElement(uuid)
			?: ProjectModule.projectHolder.project!!.getContainerLibraryElement(uuid)
	}

	/** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
	override fun getMetaGraph(uuid: UUID): MetaGraph =
		LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid)
			?: ProjectModule.projectHolder.project!!.getMetaGraph(uuid)

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? =
		LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid)
			?: ProjectModule.projectHolder.project?.getOptionalMetaGraph(uuid)

	/** Checks whether a [MetaGraph] with [uuid] exists in this [MetaGraphRepository]. */
	override fun containsMetaGraph(uuid: UUID): Boolean =
		LibraryModule.libraryHolder.library.containsMetaGraph(uuid) || ProjectModule.projectHolder.project?.containsMetaGraph(uuid) == true

	override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean {
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

	override fun createBundle(metaGraph: MetaGraph): MetaGraphBundle {
		var referencesSystemLibrary = false
		return MetaGraphBundle()
			.add(metaGraph)
			.also { bundle ->
				ContainerLibraryElementCollector(this)
					.collect(metaGraph.graph.graphView.graph!!)
					.forEach {
						if (isFromSystemLibrary(it)) {
							referencesSystemLibrary = true
						} else {
							bundle.add(getMetaGraph(it))
						}
					}
				if (referencesSystemLibrary) {
					bundle.referencedSystemLibrary = LibraryModule.libraryHolder.library.uuid
				}
			}
	}

	private fun isFromSystemLibrary(uuid: UUID): Boolean {
		if (LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid) != null) {
			return LibraryModule.libraryHolder.library.isSystem
		}
		return false
	}
}