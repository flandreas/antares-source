package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.Expression
import ch.scorpion.antares.hdl.expression.NetExpression
import ch.scorpion.antares.hdl.expression.NotExpression
import ch.scorpion.antares.hdl.expression.OperationExpression
import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.gate.NonUnaryLogicGate
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType
import ch.scorpion.antares.model.gate.UnaryLogicGate
import ch.scorpion.antares.model.gate.UnaryLogicGateType
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * The context for creating [HDLCircuit]s.
 * @property repository used for accessing [MetaGraph]s when creating [HDLCircuit]s
 */
class HDLModel(
	circuit: DigitalGraph,
	private val repository: MetaGraphRepository
) {

	/**
	 * Keeps track of the one and only [HDLCircuit] model created for a [DigitalGraph]
	 * with a particular [UUID].
	 */
	private val hdlCircuits = mutableMapOf<UUID, HDLCircuit>()

	val main: HDLCircuit = HDLCircuit(circuit, "main", this)

	init {
		hdlCircuits[main.uuid] = main
	}

	fun create(): HDLModel {
		return this
	}

	fun createNode(vertice: Vertice, parent: HDLCircuit): AbstractHDLNode {
		return when (vertice) {
			is SubGraphVerticeRef -> {
				val hdlCircuit = hdlCircuits.computeIfAbsent(vertice.graphUUID!!) {
					HDLCircuit(vertice.getGraph(repository) as DigitalGraph, it.id, this)
				}
				HDLCircuitNode(hdlCircuit).also { addInputsOutputs(it, vertice, parent) }.createExpressions()
			}
			is UnaryLogicGate -> {
				when (vertice.gateType) {
					UnaryLogicGateType.Not -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(NetExpression(it.inputs.first().net!!))
						}
					}
					UnaryLogicGateType.Buffer -> {
						createExpression(vertice, parent).also {
							it.expression = NetExpression(it.inputs.first().net!!)
						}
					}
					else -> throw HDLException("Circuit element ${vertice.type} doesn't support HDL")
				}
			}
			is NonUnaryLogicGate -> {
				when (vertice.gateType) {
					NonUnaryLogicGateType.And -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.AND)
						}
					}
					NonUnaryLogicGateType.Or -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.OR)
						}
					}
					NonUnaryLogicGateType.Xor -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.XOR)
						}
					}
					NonUnaryLogicGateType.Nor -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.OR))
						}
					}
					NonUnaryLogicGateType.Nand -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.AND))
						}
					}
					NonUnaryLogicGateType.Xnor -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.XOR))
						}
					}
					else -> throw HDLException("Circuit element ${vertice.type} doesn't support HDL")
				}
			}
			else -> throw HDLException("Circuit element ${vertice.type} doesn't support HDL")
		}
	}

	private fun createExpression(vertice: Vertice, parent: HDLCircuit): HDLNodeAssignment {
		val node = HDLNodeAssignment(vertice.type)
		addInputsOutputs(node, vertice, parent)
		return node
	}

	private fun createOperation(inputs: Collection<HDLPort>, op: OperationExpression.Operation): Expression {
		val list = mutableListOf<Expression>()
		for (input in inputs) {
			input.net?.let {
				list.add(NetExpression(it))
			}
		}
		return OperationExpression(op, list)
	}

	private fun addInputsOutputs(node: AbstractHDLNode, vertice: Vertice, hdlCircuit: HDLCircuit) {
		for (port in vertice.getPorts()) {
			val net = hdlCircuit.getHDLNetOfPort(port)
			when (port.portType) {
				PortType.INPUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.IN, net))
				PortType.OUTPUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.OUT, net))
				PortType.INOUT -> TODO()
			}
		}
	}

	private fun portName(port: Port<*>): String =
		if (StringUtils.isBlank(port.name)) {
			"p${port.portId}"
		} else {
			port.name!!
		}
}