package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.expression.*
import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.hdl.vhdl.VHDLTemplate
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.gate.*
import io.antarescircuit.antares.model.input.DipSwitch
import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.net.Ground
import io.antarescircuit.antares.model.net.Power
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef

/**
 * The context for creating [HDLCircuit]s.
 */
class HDLModel(
	circuit: DigitalGraph,
	renaming: HDLRenaming
) {
	private val renaming: HDLRenaming = RenameSingleCheck(renaming)

	/**
	 * Keeps track of the one and only [HDLCircuit] model created for a [DigitalGraph]
	 * with a particular [UUID].
	 */
	private val hdlCircuits = mutableMapOf<UUID, HDLCircuit>()

	val main: HDLCircuit = HDLCircuit(circuit, renaming.checkName(circuit.name.value), this)

	init {
		hdlCircuits[main.uuid] = main
	}

	fun create(): HDLModel {
		return this
	}

	fun createNode(vertice: Vertice, parent: HDLCircuit): AbstractHDLNode {
		return when (vertice) {
			is SubGraphVerticeRef -> {
				// computeIfAbsent() would generate ConcurrentModificationException
				var hdlCircuit = hdlCircuits[vertice.graphUUID!!]
				if (hdlCircuit == null) {
					val digitalGraph = vertice.getGraph() as DigitalGraph
					if (digitalGraph.purelyScripted) {
						throw HDLException(Translations.getString("antares.vhdl.purelyScriptedNotSupported.error.txt", digitalGraph.name.value))
					}
					hdlCircuit = HDLCircuit(digitalGraph, renaming.checkName(vertice.getGraph().name.value), this)
					hdlCircuits[vertice.graphUUID!!] = hdlCircuit
				}
				HDLCircuitNode(hdlCircuit).also { addInputsOutputs(it, vertice, parent) }.createExpressions()
			}
			is UnaryLogicGate -> {
				when (vertice.gateType) {
					UnaryLogicGateType.Not -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(NetExpression(it.inputs.first().net!!), vertice.propagationDelay.value)
						}
					}
					UnaryLogicGateType.Buffer -> {
						createExpression(vertice, parent).also {
							it.expression = NetExpression(it.inputs.first().net!!, vertice.propagationDelay.value)
						}
					}
					else -> throw HDLException(Translations.getString("antares.vhdl.elementNotSupported.error.txt", vertice.type))
				}
			}
			is NonUnaryLogicGate -> {
				when (vertice.gateType) {
					NonUnaryLogicGateType.And -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.AND, vertice.propagationDelay.value)
						}
					}
					NonUnaryLogicGateType.Or -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.OR, vertice.propagationDelay.value)
						}
					}
					NonUnaryLogicGateType.Xor -> {
						createExpression(vertice, parent).also {
							it.expression = createOperation(it.inputs, OperationExpression.Operation.XOR, vertice.propagationDelay.value)
						}
					}
					NonUnaryLogicGateType.Nor -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.OR, 0) , vertice.propagationDelay.value)
						}
					}
					NonUnaryLogicGateType.Nand -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.AND, 0), vertice.propagationDelay.value)
						}
					}
					NonUnaryLogicGateType.Xnor -> {
						createExpression(vertice, parent).also {
							it.expression = NotExpression(createOperation(it.inputs, OperationExpression.Operation.XOR, 0), vertice.propagationDelay.value)
						}
					}
					else -> throw HDLException(Translations.getString("antares.vhdl.elementNotSupported.error.txt", vertice.type))
				}
			}
			is Constant -> {
				createExpression(vertice, parent).also {
					it.expression = ConstantExpression(vertice.valueSignal)
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
			else -> {
				BuiltInNode(vertice::class.simpleName!!, vertice.type).also {
					addInputsOutputs(it, vertice, parent)
					it.createExpressions()
					it.setAttribute(VHDLTemplate.ATTR_VERTICE, vertice)
				}
			}
		}
	}

	fun renameLabels() {
		for (c in hdlCircuits.values) {
			c.rename(this.renaming)
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
			val logic = if (vertice is AbstractLogicGate) {
				port.logic
			} else {
				Logic.POSITIVE
			}
			when (port.portType) {
				PortType.INPUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.IN, net, port.bitWidth, logic))
				PortType.OUTPUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.OUT, net, port.bitWidth))
				// InOut not yet really supported by HDL
				PortType.INOUT -> node.addPort(HDLPort(portName(port), HDLPort.Direction.OUT, net, port.bitWidth))
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