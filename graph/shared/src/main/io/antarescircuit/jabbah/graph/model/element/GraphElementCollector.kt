package io.antarescircuit.jabbah.graph.model.element

import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.EmptyHierarchyVisitor
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.graph.MetaGraphRepository
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import kotlin.reflect.KClass

data class GraphElementCollectorResult(
	val deep: Collection<GraphElementCollectorResultEntry>,
	val flat: Collection<GraphElementCollectorResultEntry>,
	val immediate: Collection<GraphElementCollectorResultEntry>
)

data class GraphElementCollectorResultEntry(
	val id: Any,
	val clazz: KClass<GraphElement>,
	val name: String,
	val isScripted: Boolean,
	val graphType: GraphType?,
	val font: Font,
	var count: Int = 0
) : Comparable<GraphElementCollectorResultEntry> {

	val richText: RichTextDrawable by lazy {
		RichTextDrawable.of(name, font)
	}

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
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder,
	private val font: Font
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

	/**
	 * Maps [GraphElement] IDs to immediate statistic information for that [Graph], including the number of occurrences.
	 * Build-in components use their type name as ID, while the ID of a [SubGraphVerticeRef] is their [UUID].
	 */
	private val immediateEntries = mutableMapOf<Any, GraphElementCollectorResultEntry>()


	/** Counts the number of occurrence of all inner [Graph]s and prints the result to standard output.*/
	fun collect(graph: Graph): GraphElementCollectorResult {
		deepEntries.clear()
		flatEntries.clear()
		immediateEntries.clear()

		graph.bind(true, repository)

		graph.accept(DeepGraphVisitor())
		graph.accept(FlatGraphVisitor())
		graph.accept(ImmediateGraphVisitor())

		return GraphElementCollectorResult(deepEntries.values, flatEntries.values, immediateEntries.values)
	}

	private fun countDeep(id: Any, clazz: KClass<GraphElement>, name: String, isScripted: Boolean, graphType: GraphType?) {
		count(id, clazz, name, isScripted, graphType, deepEntries)
	}

	private fun countFlat(id: Any, clazz: KClass<GraphElement>, name: String, isScripted: Boolean, graphType: GraphType?) {
		count(id, clazz, name, isScripted, graphType, flatEntries)
	}

	private fun countImmediate(id: Any, clazz: KClass<GraphElement>, name: String, isScripted: Boolean, graphType: GraphType?) {
		count(id, clazz, name, isScripted, graphType, immediateEntries)
	}

	private fun count(
		id: Any,
		clazz: KClass<GraphElement>,
		name: String,
		isScripted: Boolean,
		graphType: GraphType?,
		entries: MutableMap<Any, GraphElementCollectorResultEntry>
	) {
		var entry = entries[id]
		if (entry == null) {
			entry = GraphElementCollectorResultEntry(id, clazz, name, isScripted, graphType, font)
			entries[id] = entry
		}
		entry.increment()
	}

	private inner class DeepGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				val graphType = node?.getGraphIfPresent()?.type
				countDeep(node.graphUUID!!, node::class as KClass<GraphElement>, node.graphName.value, node.getGraphIfPresent()?.script != null, graphType)
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				val graphType = (node as? SubGraphVerticeRef)?.getGraphIfPresent()?.type
				countDeep(node.type, node::class as KClass<GraphElement>, node.type, isScripted = false, graphType)
			}
			return true
		}
	}

	private inner class FlatGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				val graphType = node.getGraphIfPresent()?.type
				countFlat(node.graphUUID!!, node::class as KClass<GraphElement>, node.graphName.value, node.getGraphIfPresent()?.script != null, graphType)
				if (node.getGraphIfPresent()!!.script != null) {
					return false
				}
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				val graphType = (node as? SubGraphVerticeRef)?.getGraphIfPresent()?.type
				countFlat(node.type, node::class as KClass<GraphElement>, node.type, isScripted = false, graphType)
			}
			return true
		}
	}

	private inner class ImmediateGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				val graphType = node.getGraphIfPresent()?.type
				countImmediate(node.graphUUID!!, node::class as KClass<GraphElement>, node.graphName.value, node.getGraphIfPresent()?.script != null, graphType)
				return false
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				val graphType = (node as? SubGraphVerticeRef)?.getGraphIfPresent()?.type
				countImmediate(node.type, node::class as KClass<GraphElement>, node.type, isScripted = false, graphType)
			}
			return true
		}
	}
}