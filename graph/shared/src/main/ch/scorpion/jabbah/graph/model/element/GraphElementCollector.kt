package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.reflect.KClass

data class GraphElementCollectorResult(
	val deep: Collection<GraphElementCollectorResultEntry>,
	val flat: Collection<GraphElementCollectorResultEntry>
)

data class GraphElementCollectorResultEntry(
	val id: Any,
	val clazz: KClass<GraphElement>,
	val name: String,
	val isScripted: Boolean,
	var count: Int = 0
) : Comparable<GraphElementCollectorResultEntry> {

	fun increment() {
		count += 1
	}

	override fun compareTo(other: GraphElementCollectorResultEntry): Int =
		count.compareTo(other.count)
}

/**
 * A [GraphElementCollector] recursively traverses a [Graph] and collects all its [GraphElement]s.
 * Can be used for debugging, for gathering statistical information, or for any other funny purpose.
 */
class GraphElementCollector(
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder
) {

	/**
	 * Maps [GraphElement] IDs to deep statistic information for that [Graph], including the number of occurrences.
	 * Build-in components use their type name as ID, while the ID of a [SubGraphVerticeRef] is their [UUID].
	 */
	private val deepEntries = mutableMapOf<Any, GraphElementCollectorResultEntry>()

	/**
	 * Maps [GraphElement] IDs to flat statistic information for that [Graph], including the number of occurrences.
	 * Build-in components use their type name as ID, while the ID of a [SubGraphVerticeRef] is their [UUID].
	 */
	private val flatEntries = mutableMapOf<Any, GraphElementCollectorResultEntry>()


	/** Counts the number of occurrence of all inner [Graph]s and prints the result to standard output.*/
	fun collect(graph: Graph): GraphElementCollectorResult {
		deepEntries.clear()
		flatEntries.clear()

		graph.bind(true, repository)

		graph.accept(DeepGraphVisitor())
		graph.accept(FlatGraphVisitor())

		return GraphElementCollectorResult(deepEntries.values, flatEntries.values)
	}

	private fun countDeep(id: Any, clazz: KClass<GraphElement>, name: String, isScripted: Boolean) {
		count(id, clazz, name, isScripted, deepEntries)
	}

	private fun countFlat(id: Any, clazz: KClass<GraphElement>, name: String, isScripted: Boolean) {
		count(id, clazz, name, isScripted, flatEntries)
	}

	private fun count(
		id: Any,
		clazz: KClass<GraphElement>,
		name: String,
		isScripted: Boolean,
		entries: MutableMap<Any, GraphElementCollectorResultEntry>
	) {
		var entry = entries[id]
		if (entry == null) {
			entry = GraphElementCollectorResultEntry(id, clazz, name, isScripted)
			entries[id] = entry
		}
		entry.increment()
	}

	private inner class DeepGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				countDeep(node.graphUUID!!, node::class as KClass<GraphElement>, node.name!!, node.getGraphIfPresent()?.script != null)
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				countDeep(node.type, node::class as KClass<GraphElement>, node.type, isScripted = false)
			}
			return true
		}
	}

	private inner class FlatGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				countFlat(node.graphUUID!!, node::class as KClass<GraphElement>, node.name!!, node.getGraphIfPresent()?.script != null)
				if (node.getGraphIfPresent()!!.script != null) {
					return false
				}
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				countFlat(node.type, node::class as KClass<GraphElement>, node.type, isScripted = false)
			}
			return true
		}
	}
}