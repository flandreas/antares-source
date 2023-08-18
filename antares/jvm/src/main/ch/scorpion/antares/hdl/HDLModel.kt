package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.PortType
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

	fun createNode(elem: GraphElement, parent: HDLCircuit): AbstractHDLNode {
		return if (elem is SubGraphVerticeRef) {
			val hdlCircuit = hdlCircuits.computeIfAbsent(elem.graphUUID!!) {
				HDLCircuit(elem.getGraph(repository) as DigitalGraph, it.id, this)
			}
			HDLCircuitNode(hdlCircuit).also { addInputsOutputs(it, elem, parent) }.createExpressions()
		} else {
			throw HDLException("Circuit element ${elem.type} doesn't support HDL")
		}
	}

	private fun addInputsOutputs(node: AbstractHDLNode, elem: SubGraphVerticeRef, hdlCircuit: HDLCircuit) {
		for (port in elem.getPorts()) {
			val net = hdlCircuit.getHDLNetOfPort(port)
			when (port.portType) {
				PortType.INPUT -> node.addPort(HDLPort(port.name!!, HDLPort.Direction.IN, net))
				PortType.OUTPUT -> node.addPort(HDLPort(port.name!!, HDLPort.Direction.OUT, net))
				PortType.INOUT -> TODO()
			}
		}
	}
}