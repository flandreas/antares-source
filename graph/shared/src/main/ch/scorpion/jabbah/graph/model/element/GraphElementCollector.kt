package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.module.GraphModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

data class GraphElementCollectorResult(
	val deep: String,
	val flat: String
)

/**
 * A [GraphElementCollector] recursively traverses a [Graph] and collects all its [GraphElement]s.
 * Can be used for debugging, for gathering statistical information, or for any other funny purpose.
 */
class GraphElementCollector(
	private val repository: MetaGraphRepository = GraphModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator
) {

	/** Maps [Graph] names to deep statistic information for that [Graph], including the number of occurrences.*/
	private val deepEntries = mutableMapOf<String, Entry>()

	/** Maps [Graph] names to flat statistic information for that [Graph], including the number of occurrences.*/
	private val flatEntries = mutableMapOf<String, Entry>()


	/** Counts the number of occurrence of all inner [Graph]s and prints the result to standard output.*/
	fun collect(graph: Graph): GraphElementCollectorResult {
		deepEntries.clear()
		flatEntries.clear()

		graph.bind(true, repository, storableCreator)

		graph.accept(DeepGraphVisitor())
		graph.accept(FlatGraphVisitor())

		return GraphElementCollectorResult(
			printToString(deepEntries),
			printToString(flatEntries))
	}

	private fun countDeep(name: String) {
		count(name, deepEntries)
	}

	private fun countFlat(name: String) {
		count(name, flatEntries)
	}

	private fun count(name: String, entries: MutableMap<String, Entry>) {
		var entry = entries[name]
		if (entry == null) {
			entry = Entry(name)
			entries[name] = entry
		}
		entry.increment()
	}

	private fun printToString(entries: MutableMap<String, Entry>): String {
		val builder = StringBuilder()
		entries.values.toList().sortedDescending().forEach { it.print(builder) }
		return builder.toString()
	}

	private fun getName(ref: SubGraphVerticeRef): String {
		val name = "[${ref.name}]"
		return ref.getGraphIfPresent()?.script?.let { "*$name" } ?: name
	}

	private data class Entry(private val name: String, private var count: Int = 0) : Comparable<Entry> {

		fun increment() {
			count += 1
		}

		fun print(builder: StringBuilder) {
			builder.append("$count: $name\n")
		}

		override fun compareTo(other: Entry): Int {
			return count.compareTo(other.count)
		}
	}

	private inner class DeepGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				countDeep(getName(node))
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				countDeep(node.type)
			}
			return true
		}
	}

	private inner class FlatGraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				countFlat(getName(node))
				if (node.getGraphIfPresent()!!.script != null) {
					return false
				}
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				countFlat(node.type)
			}
			return true
		}
	}
}