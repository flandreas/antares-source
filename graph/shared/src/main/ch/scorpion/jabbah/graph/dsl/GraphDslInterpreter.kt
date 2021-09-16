package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.ReturnStatement

open class GraphDslInterpreter(
	node: Node,
	memory: Memory = Memory()
) : Interpreter(node, memory) {

	constructor(parser: GraphDslParser): this(parser.parse())
	constructor(program: String): this(GraphDslParser(program))

	private val initStatementFinder = InitStatementFinder()

	fun executionStarted() {
		// Execute only InitStatement (if any) on execution start
		initStatementFinder.findIn(node)?.let {
			init(it)
		}
	}

	override fun interpret(node: Node): Any =
		when (node) {
			// Skip InitStatement on acting
			is InitStatement -> { }
			else -> super.interpret(node)
		}

	private fun init(node: InitStatement): Any {
		interpret(node.block)
		return interpret(ReturnStatement(node.location, null))
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