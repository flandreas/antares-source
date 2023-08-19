package ch.scorpion.antares.hdl

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.*

/** A representation of a circuit [Graph] suitable for exporting by a HDL creator.*/
class HDLCircuit(
	val circuit: DigitalGraph,
	val elementName: String,
	private val model: HDLModel
) {
	private val _nodes = mutableListOf<AbstractHDLNode>()
	val nodes: List<AbstractHDLNode> get() = _nodes

	private val ports = mutableListOf<HDLPort>()

	private val netMap: MutableMap<Net<*>, HDLNet> = mutableMapOf()

	val name: String get() = circuit.name.value

	val nets: Collection<HDLNet> get() = netMap.values

	val uuid: UUID get() = circuit.uuid

	val portsCount: Int get() = ports.size

	val circuitNodes: List<HDLCircuitNode> get() = nodes.filterIsInstance<HDLCircuitNode>()

	/**
	 * Returns the [HDLPort]s sending a signal into this [HDLCircuit].
	 * From inside, the [HDLPort] is [HDLPort.Direction.OUT], because it defines a value.
	 */
	val inputs: List<HDLPort> get() = ports.filter { it.direction == HDLPort.Direction.OUT }

	/**
	 * Returns the [HDLPort]s sending a signal out of this [HDLCircuit].
	 * From inside, the [HDLPort] is [HDLPort.Direction.IN], because it reads the value to be sent outwards.
	 */
	val outputs: List<HDLPort> get() = ports.filter { it.direction == HDLPort.Direction.IN }

	val inOuts: List<HDLPort> get() = ports.filter { it.direction == HDLPort.Direction.INOUT }

	init {
		createNodes()

		for (input in inputs) {
			input.net?.setIsInput(input.name)
		}
		for (output in outputs) {
			output.net?.let {
				if (it.needsVariable) {
					it.setIsOutput(output.name)
				}
			}
		}

		nameNets()
	}

	private fun createNodes() {
		for (elem in circuit.elements.filterIsInstance<Vertice>()) {
			if (elem is GraphInput<*> && elem.portType == PortType.INPUT) {
				val port = elem.getPort<Any>() as DigitalPort
				addPort(HDLPort(elem.name!!, HDLPort.Direction.OUT, getHDLNetOfPort(port), port.bitWidth))
			} else if (elem is GraphOutput<*> && elem.portType == PortType.OUTPUT) {
				val port = elem.getPort<Any>() as DigitalPort
				addPort(HDLPort(elem.name!!, HDLPort.Direction.IN, getHDLNetOfPort(port), port.bitWidth))
			} else if (elem is Concentrator) {
				_nodes.add(ManyToOneNode(model.createNode(elem, this), elem.bitWidth, elem.branchCount))
			}  else {
				_nodes.add(model.createNode(elem, this))
			}
		}
	}

	fun getHDLNetOfPort(port: DigitalPort): HDLNet? =
		port.net?.let { netMap.computeIfAbsent(it) { HDLNet(bitWidth = port.bitWidth) } }

	private fun addPort(port: HDLPort) {
		ports.add(port)
	}

	private fun nameNets() {
		var index = 0
		netMap.values.filter { StringUtils.isBlank(it.name) }.forEach {
			it.name = "s${index++}"
		}
	}
}