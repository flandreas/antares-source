package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.expression.*
import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.hdl.vhdl.VHDLTemplate
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.*
import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.net.*
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignalFactory
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
							it.expression = NotExpression(NetExpression(it.inputs.first().net!!), vertice.propagationDelay)
						}
					}
					UnaryLogicGateType.Buffer -> {
						createExpression(vertice, parent).also {
							it.expression = NetExpression(it.inputs.first().net!!, vertice.propagationDelay)
						}
					}
					else -> throw HDLException("Circuit element ${vertice.type} doesn't support HDL")
				}
			}
			is NonUnaryLogicGate -> {
				when (vertice.gateType) {
					NonUnaryLogicGateType.And -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.AND, vertice.propagationDelay)
						}
					}
					NonUnaryLogicGateType.Or -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.OR, vertice.propagationDelay)
						}
					}
					NonUnaryLogicGateType.Xor -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.XOR, vertice.propagationDelay)
						}
					}
					NonUnaryLogicGateType.Nor -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.OR, 0) , vertice.propagationDelay)
						}
					}
					NonUnaryLogicGateType.Nand -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.AND, 0), vertice.propagationDelay)
						}
					}
					NonUnaryLogicGateType.Xnor -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.XOR, 0), vertice.propagationDelay)
						}
					}
					else -> throw HDLException("Circuit element ${vertice.type} doesn't support HDL")
				}
			}
			is Constant -> {
				createExpression(vertice, parent).also {
					it.expression = ConstantExpression(vertice.value)
				}
			}
			is DipSwitch -> {
				createExpression(vertice, parent).also {
					it.expression = ConstantExpression(vertice.startupValue)
				}
			}
			is Power -> {
				createExpression(vertice, parent).also {
					it.expression = ConstantExpression(DigitalSignalFactory.trueValue(vertice.bitWidth))
				}
			}
			is Ground -> {
				createExpression(vertice, parent).also {
					it.expression = ConstantExpression(DigitalSignalFactory.falseValue(vertice.bitWidth))
				}
			}
			// TODO: Consider extracting attribute setting logic to external registry
			is TriStateBufferGate -> {
				BuiltInNode(vertice::class.simpleName!!).also {
					addInputsOutputs(it, vertice, parent)
					it.createExpressions()
					it.setAttribute(VHDLTemplate.ATTR_BIT_WIDTH, vertice.bitWidth.width)
					it.setAttribute(VHDLTemplate.ATTR_NEGATIVE, vertice.enableLogic == Logic.NEGATIVE)
				}
			}
			else -> {
				BuiltInNode(vertice::class.simpleName!!).also {
					addInputsOutputs(it, vertice, parent)
					it.createExpressions()
				}
			}
		}
	}

	private fun createExpression(vertice: Vertice, parent: HDLCircuit): HDLNodeAssignment {
		val node = HDLNodeAssignment(vertice.type)
		addInputsOutputs(node, vertice, parent)
		return node
	}

	private fun createOperation(
		inputs: Collection<HDLPort>,
		op: OperationExpression.Operation,
		delay: Long
	): Expression {
		val list = mutableListOf<Expression>()
		for (input in inputs) {
			input.net?.let {
				if (input.logic == Logic.NEGATIVE) {
					list.add(NotExpression(NetExpression(it)))
				} else {
					list.add(NetExpression(it))
				}
			}
		}
		return OperationExpression(op, list, delay)
	}

	private fun addInputsOutputs(node: AbstractHDLNode, vertice: Vertice, hdlCircuit: HDLCircuit) {
		for (port in vertice.getPorts().map { it as DigitalPort }) {
			val net = hdlCircuit.getHDLNetOfPort(port)
			when (port.portType) {
				PortType.INPUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.IN, net, port.bitWidth, port.logic))
				PortType.OUTPUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.OUT, net, port.bitWidth, port.logic))
				// InOut not yet really supported by HDL
				PortType.INOUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.OUT, net, port.bitWidth, port.logic))
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