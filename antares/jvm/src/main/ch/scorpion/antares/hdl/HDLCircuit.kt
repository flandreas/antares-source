package ch.scorpion.antares.hdl

import ch.scorpion.antares.hdl.vhdl.HDLException
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.net.Splitter
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
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

	var entityName: String = elementName
		private set

	init {
		LOG.debug("Create HDLCircuit for ${circuit.name} (${circuit.uuid.id})")

		createTunnelNets()
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
			} else if (elem is Clock) {
				if (StringUtils.isBlank(elem.name)) {
					throw HDLException(Translations.getString("antares.vhdl.missingClockName.error.txt"))
				}
				addPort(HDLPort(elem.name!!, HDLPort.Direction.OUT, getHDLNetOfPort(elem.getPort<DigitalSignal>() as DigitalPort), BitWidth.BW_1))
			} else if (elem is Concentrator) {
				_nodes.add(ManyToOneNode(model.createNode(elem, this), elem.bitWidth, elem.branchCount))
			} else if (elem is Splitter) {
				_nodes.add(OneToManyNode(model.createNode(elem, this), elem.bitWidth, elem.branchCount))
			} else if (isRealElement(elem)) {
				_nodes.add(model.createNode(elem, this))
			}
		}
	}

	private fun isRealElement(vertice: Vertice): Boolean {
		return vertice !is Tunnel
	}

	fun getHDLNetOfPort(port: DigitalPort): HDLNet? =
		port.net?.let { netMap.computeIfAbsent(it) { HDLNet(bitWidth = port.bitWidth) } }

	fun getPort(name: String): HDLPort? = ports.firstOrNull { it.name.equals(name, true) }

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
		entityName = renaming.checkName(entityName)

		checkUniqueNames(ports.map { it::name })
		checkUniqueNames(nets.toSet().map { it::name })
	}

	/**
	 * Pre-allocates [HDLNet]s for all [Tunnel]s such that all [Tunnel]s with the same name
	 * end up with a single [HDLNet] connecting all related [Net]s.
	 */
	private fun createTunnelNets() {
		circuit.elements
			.filterIsInstance<Tunnel>()
			.filter { StringUtils.isNotEmpty(it.name)}
			.forEach { tunnel ->
				val visibleNet = tunnel.visiblePort.net
				if (visibleNet != null) {
					if (netMap[visibleNet] == null) {
						createTunnelNet(tunnel, HDLNet(bitWidth = tunnel.invisiblePort.bitWidth), mutableSetOf())
					}
				}
			}
	}

	private fun createTunnelNet(tunnel: Tunnel, hdlNet: HDLNet, visitedTunnels: MutableSet<Tunnel>) {
		if (visitedTunnels.contains(tunnel)) {
			return
		}
		visitedTunnels.add(tunnel)

		val visibleNet = tunnel.visiblePort.net
		if (visibleNet != null) {

			if (netMap[visibleNet] == null) {
				netMap[visibleNet] = hdlNet
			}

			// Expand to other Tunnels with the same name
			circuit.elements
				.filterIsInstance<Tunnel>()
				.filter { it !== tunnel && it.name == tunnel.name }
				.forEach { createTunnelNet(it, hdlNet, visitedTunnels) }

			// Expand to other Tunnels at the visible Port
			visibleNet.ports
				.filter { it !== tunnel.visiblePort && it.owner is Tunnel }
				.map { it.owner as Tunnel }
				.forEach { createTunnelNet(it, hdlNet, visitedTunnels) }
		}
	}

	private fun checkUniqueNames(nameProviders: Collection<() -> String>) {
		val names = mutableSetOf<String>()
		nameProviders.forEach {
			val name = it()
			if (names.contains(name)) {
				throw HDLException(Translations.getString("antares.vhdl.nonUniqueName.error.txt", name))
			}
			names.add(name)
		}
	}
}