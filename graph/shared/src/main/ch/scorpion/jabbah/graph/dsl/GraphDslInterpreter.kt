package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.RuntimeError
import ch.scorpion.jabbah.graph.model.*
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
			is PropertyPortName -> propertyPortName(node)
			is PropertyPortId -> propertyPortId(node)
			else -> super.interpret(node)
		}

	private fun init(node: InitStatement): Any = interpret(node.block)

	private fun propertyPortName(node: PropertyPortName): Any {
		val elemId = interpret(node.elemId)
		if (elemId !is Long) {
			throw RuntimeError(node.elemId.location, "Expected number")
		}
		val portName = node.portName.token.value!!
		if (params == null || params !is Graph) {
			throw RuntimeError(node.location, "No elements available")
		}
		val graphElement = (params as Graph).withId(elemId.toInt())
			?: throw RuntimeError(node.elemId.location, "Element with ID $elemId not found")

		if (graphElement !is Vertice) {
			throw RuntimeError(node.elemId.location, "Element has no ports")
		}

		if (!graphElement.hasPort(portName)) {
			throw RuntimeError(node.portName.location, "Port '$portName' not found")
		}
		val port = graphElement.getPort<Any>(portName)
		val signal = when (port.portType) {
			PortType.INPUT -> (port as InputPort).getIncomingSignal()
			PortType.OUTPUT -> (port as OutputPort).getOutgoingSignal()
			PortType.INOUT -> (port as BidirectionalPort).dominantSignal
		}

		return signal ?: RuntimeError(node.location, "No signal at port '$portName'")
	}

	private fun propertyPortId(node: PropertyPortId): Any {
		val elemId = interpret(node.elemId)
		if (elemId !is Long) {
			throw RuntimeError(node.elemId.location, "Expected number")
		}
		val portId = node.portId.token.value!!
		if (portId !is Long) {
			throw RuntimeError(node.portId.location, "Expected number")
		}
		if (params == null || params !is Graph) {
			throw RuntimeError(node.location, "No elements available")
		}
		val graphElement = (params as Graph).withId(elemId.toInt())
			?: throw RuntimeError(node.elemId.location, "Element with ID $elemId not found")

		if (graphElement !is Vertice) {
			throw RuntimeError(node.elemId.location, "Element has no ports")
		}
		if (!graphElement.hasPort(portId.toInt())) {
			throw RuntimeError(node.portId.location, "Port '$portId' not found")
		}
		val port = graphElement.getPort<Any>(portId.toInt())
		val signal = when (port.portType) {
			PortType.INPUT -> (port as InputPort).getIncomingSignal()
			PortType.OUTPUT -> (port as OutputPort).getOutgoingSignal()
			PortType.INOUT -> (port as BidirectionalPort).dominantSignal
		}

		return signal ?: RuntimeError(node.location, "No signal at port $portId")
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