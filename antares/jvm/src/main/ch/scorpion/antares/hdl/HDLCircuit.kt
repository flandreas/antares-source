package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.net.Splitter
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.*

/** A representation of a circuit [Graph] suitable for exporting by a HDL creator.*/
class HDLCircuit(
	val circuit: DigitalGraph,
	val elementName: String,
	private val model: HDLModel
) {
	companion object {
		private val LOG by logger(HDLCircuit::class)
	}

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

	private var _entityName: String = elementName
	val entityName: String = _entityName

	init {
		LOG.debug("Create HDLCircuit for ${circuit.name} (${circuit.uuid.id})")

		createNodes()

		for (input in inputs) {
			input.net?.let { net ->
				net.setIsInput(input.name)
				if (net.isInOutNet) {
					input.setInOut()
				}
			}
		}
		for (output in outputs) {
			output.net?.let { net ->
				if (net.needsVariable) {
					net.setIsOutput(output.name, net.inputs.size == 1)
				}
				if (net.isInOutNet) {
					output.setInOut()
				}
			}
		}

		nameNets()
	}

	private fun createNodes() {
		for (elem in circuit.elements.filterIsInstance<Vertice>()) {
			if (elem is DigitalCircuitInOut) {
				val port = elem.getPort<Any>() as DigitalPort
				when (elem.portType) {
					PortType.INPUT -> addPort(HDLPort(elem.name!!, HDLPort.Direction.OUT, getHDLNetOfPort(port), port.bitWidth))
					PortType.OUTPUT -> addPort(HDLPort(elem.name!!, HDLPort.Direction.IN, getHDLNetOfPort(port), port.bitWidth))
					PortType.INOUT -> addPort(HDLPort(elem.name!!, HDLPort.Direction.INOUT, getHDLNetOfPort(port), port.bitWidth))
				}
			} else if (elem is Concentrator) {
				_nodes.add(ManyToOneNode(model.createNode(elem, this), elem.bitWidth, elem.branchCount))
			} else if (elem is Splitter) {
				_nodes.add(OneToManyNode(model.createNode(elem, this), elem.bitWidth, elem.branchCount))
			} else {
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

	fun rename(renaming: HDLRenaming) {
		ports.forEach { it.rename(renaming) }
		nets.forEach { it.rename(renaming) }
		nodes.forEach { it.rename(renaming) }
		_entityName = renaming.checkName(entityName)

		checkUniqueNames(ports.map { it::name })
		checkUniqueNames(nets.map { it::name })
	}

	private fun checkUniqueNames(nameProviders: Collection<() -> String>) {
		val names = mutableSetOf<String>()
		nameProviders.forEach {
			val name = it()
			if (names.contains(name)) {
				throw HDLException("Name '$name' is not unique")
			}
			names.add(name)
		}
	}
}