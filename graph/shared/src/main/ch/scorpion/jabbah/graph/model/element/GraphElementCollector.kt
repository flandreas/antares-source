package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

/**
 * A [GraphElementCollector] recursively traverses a [Graph] and collects all its [GraphElement]s.
 * Can be used for debugging, for gathering statistical information, or for any other funny purpose.
 */
class GraphElementCollector(
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator
) {

	/** Maps [Graph] names to statistic information for that [Graph], including the number of occurrences.*/
	private val entries = mutableMapOf<String, Entry>()

	/** Counts the number of occurrence of all inner [Graph]s and prints the result to standard output.*/
	fun collect(graph: Graph): String {
		entries.clear()
		graph.bind(repository, storableCreator)
		graph.accept(GraphVisitor())
		return printToString()
	}

	private fun count(name: String) {
		var entry = entries[name]
		if (entry == null) {
			entry = Entry(name)
			entries[name] = entry
		}
		entry.increment()
	}

	private fun printToString(): String {
		val builder = StringBuilder()
		entries.values.toList().sortedDescending().forEach { it.print(builder) }
		return builder.toString()
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

	private inner class GraphVisitor : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			if (node is SubGraphVerticeRef) {
				count("""[${node.name}]""")
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			if (node is GraphElement) {
				count(node.type)
			}
			return true
		}
	}
}