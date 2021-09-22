package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * Expects the [Graph] to be provided as "params in [interpret] whose [GraphElements][GraphElement]
 * are accessed as property, or `null` if this [GraphDslInterpreter] doesn't run in the context of a [Graph]
 * (for example when executing a [SubGraphVerticeRef]'s execution script).
 */
open class GraphDslInterpreter(
	node: Node,
	memory: Memory = Memory()
) : Interpreter(node, memory) {

	constructor(parser: GraphDslParser): this(parser.parse())
	constructor(program: String): this(GraphDslParser(program))

	private val initStatementFinder = InitStatementFinder()

	fun executionStarted(): Any {
		// Execute only InitStatement (if any) on execution start
		return initStatementFinder.findIn(node)?.let {
			init(it)
		} ?: 0
	}

	override fun interpret(node: Node): Any =
		when (node) {
			// Skip InitStatement on acting
			is InitStatement -> { }
			is Property -> property(node)
			else -> super.interpret(node)
		}

	private fun init(node: InitStatement): Any = interpret(node.block)

	private fun property(node: Property): Any {
		val id = interpret(node.id)
		if (id !is Long) {
			throw RuntimeError(node.id.location, "Expected number")
		}
		val name = node.name.token.value!!
		if (params == null || params !is Graph) {
			throw RuntimeError(node.location, "No graph elements available")
		}
		val graphElement = (params as Graph).getGraphPort<Any>(name)
			?: throw RuntimeError(node.name.location, "Graph element '$name' not found")

		if (graphElement.signal == null) {
			throw RuntimeError(node.location, "No signal at graph element '$name'")
		}
		return graphElement.signal!!
	}

	private class InitStatementFinder : EmptyHierarchyVisitor() {
		private var initStatement: InitStatement? = null

		fun findIn(node: Node): InitStatement? {
			initStatement = null
			node.accept(this)
			return initStatement
		}

		override fun visitEnter(node: Any): Boolean {
			if (node is InitStatement) {
				initStatement = node
				return false
			}
			return true
		}
	}
}