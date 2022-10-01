package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.element.ContainerLibraryElementCollector
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * Contains the results of performing [LibraryReferenceEvaluation.calculate].
 *
 * Serves for checking whether a [Library] import can safely be removed from a master [Library],
 * either because this won't create dangling references, or because all dangling references can
 * be resolved by importing the specified system [Libraries][Library] to replace the removed
 * [Library].
 *
 * @param hasNonSystemReferences `true` if any [MetaGraph] in the master [Library] references
 * an imported non-system [Library]
 * @param systemReferences the [UUIDs][UUID] of all referenced system [Libraries][Library]. Is empty
 * if [hasNonSystemReferences] is `true`
 */
class LibraryReferenceEvaluation(
	val hasNonSystemReferences: Boolean,
	val systemReferences: Set<UUID>
) {

	companion object {

		/**
		 * Determines whether [master] contains a [MetaGraph] with a reference to any [MetaGraph] in [target]
		 * (or any [Library] imported by [target]).
		 *
		 * This check can be costly, because every [MetaGraph] in the current [Library] has to be
		 * read and scanned for [SubGraphVerticeRefs][SubGraphVerticeRef] that would become
		 * broken when removing [target] from the imports.
		 */
		fun calculate(master: Library, target: Library): LibraryReferenceEvaluation {
			val systemReferences = mutableSetOf<UUID>()

			for (metaGraphId in master.metaGraphIds) {
				val metaGraph = master.getMetaGraph(metaGraphId)
				ContainerLibraryElementCollector()
					.collect(metaGraph.graph.model!!)
					.asUuids()
					.forEach { ref ->
						val elem = master.getContainerLibraryElement(ref)
						if (elem != null && target.expandedImports.libraries.map { it.uuid }.any { it == elem.library!!.uuid }) {
							if (elem.library!!.isSystem) {
								systemReferences.add(elem.library!!.uuid)
							} else {
								return LibraryReferenceEvaluation(true, emptySet())
							}
						}
					}

			}
			return LibraryReferenceEvaluation(false, systemReferences)
		}
	}
}